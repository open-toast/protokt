/*
 * Copyright (c) 2019 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.protokt.codegen.generate

import arrow.core.memoize
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.generate.WellKnownTypes.wrapWithWellKnownInterception
import com.toasttab.protokt.codegen.util.ClassLookup.converters
import com.toasttab.protokt.codegen.util.ClassLookup.getClass
import com.toasttab.protokt.codegen.util.FieldType
import com.toasttab.protokt.codegen.util.GeneratorContext
import com.toasttab.protokt.codegen.util.StandardField
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.BytesSlice
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception != null

    private fun <T> StandardField.withWrapper(
        wrapOption: String?,
        pkg: String,
        ctx: GeneratorContext,
        ifWrapped: (KClass<*>, KClass<*>) -> T
    ) =
        wrapOption?.let { wrap ->
            ifWrapped(
                getClass(inferClassName(wrap, pkg), ctx),
                protoTypeName.takeIf { it.isNotEmpty() }
                    ?.let { getClass(className, ctx) }
                    // Protobuf primitives have no typeName
                    ?: requireNotNull(type.kotlinRepresentation) {
                        "no kotlin representation for type of " +
                            "$fieldName: $type"
                    }
            )
        }

    private fun <R> StandardField.withWrapper(
        ctx: Context,
        ifWrapped: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        withWrapper(
            wrapWithWellKnownInterception,
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifWrapped
        )

    fun interceptSizeof(
        f: StandardField,
        s: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx) { wrapper, wrapped ->
            if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                s
            } else {
                interceptValueAccess(f, ctx, s)
            }
        } ?: interceptValueAccess(f, ctx, s)

    fun interceptFieldSizeof(
        f: StandardField,
        s: CodeBlock,
        ctx: Context
    ) =
        f.withWrapper(ctx) { wrapper, wrapped ->
            if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                CodeBlock.of("%T.sizeof(%L)", converterTypeName(wrapper, wrapped, ctx), s)
            } else {
                CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s))
            }
        } ?: CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s))

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        s: CodeBlock = CodeBlock.of("%N", f.fieldName)
    ): CodeBlock =
        f.withWrapper(ctx) { wrapper, wrapped ->
            CodeBlock.of(
                "%T.unwrap(%L)",
                converterTypeName(wrapper, wrapped, ctx),
                s
            )
        } ?: s

    private fun interceptDeserializedValue(f: StandardField, s: CodeBlock, ctx: Context) =
        wrapper(f, ctx)?.let { wrapField(it, s) } ?: s

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.wrap(%L)", wrapName, arg)

    fun wrapper(f: StandardField, ctx: Context) =
        f.withWrapper(ctx) { wrapper, wrapped ->
            converterTypeName(wrapper, wrapped, ctx)
        }

    fun interceptReadFn(f: StandardField, s: CodeBlock) =
        if (f.bytesSlice) {
            CodeBlock.of("readBytesSlice()")
        } else {
            s
        }

    fun interceptDefaultValue(f: StandardField, s: CodeBlock, ctx: Context) =
        if (f.bytesSlice) {
            CodeBlock.of("%T.empty()", BytesSlice::class)
        } else {
            if (f.type == FieldType.MESSAGE && !f.repeated) {
                s
            } else {
                interceptDeserializedValue(f, s, ctx)
            }
        }

    fun interceptTypeName(f: StandardField, t: TypeName, ctx: Context): TypeName =
        if (f.bytesSlice) {
            BytesSlice::class.asTypeName()
        } else {
            f.withWrapper(ctx, wrapperTypeName()) ?: t
        }

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun <R> StandardField.withKeyWrap(
        ctx: Context,
        ifWrapped: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.key?.withWrapper(
            options.protokt.keyWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifWrapped
        )

    fun interceptMapKeyTypeName(f: StandardField, t: TypeName, ctx: Context) =
        f.withKeyWrap(ctx, wrapperTypeName()) ?: t

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx) { wrapper, wrapped ->
            converter(wrapper, wrapped, ctx)::class.asTypeName()
        }

    private fun <R> StandardField.withValueWrap(
        ctx: Context,
        ifWrapped: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.value?.withWrapper(
            options.protokt.valueWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifWrapped
        )

    fun interceptMapValueTypeName(f: StandardField, t: TypeName, ctx: Context) =
        f.withValueWrap(ctx, wrapperTypeName()) ?: t

    fun mapValueConverter(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx) { wrapper, wrapped ->
            converterTypeName(wrapper, wrapped, ctx)
        }

    private fun converterTypeName(wrapper: KClass<*>, wrapped: KClass<*>, ctx: Context) =
        converter(wrapper, wrapped, ctx)::class.asTypeName()

    private fun wrapperTypeName() =
        fun(wrapper: KClass<*>, _: Any) =
            wrapper.asTypeName()

    private fun converter(wrapper: KClass<*>, wrapped: KClass<*>, ctx: Context) =
        converter(wrapper, wrapped, ctx.info.context)

    val converter = { wrapper: KClass<*>, wrapped: KClass<*>, ctx: GeneratorContext ->
        val converters =
            converters(ctx.classpath)
                .filter { it.wrapper == wrapper && it.wrapped == wrapped }

        require(converters.isNotEmpty()) {
            "${ctx.fdp.name}: No converter found for wrapper type " +
                "${wrapper.qualifiedName} from type ${wrapped.qualifiedName}"
        }

        converters
            .filterNot { it::class.hasAnnotation<Deprecated>() }
            .firstOrNull() ?: converters.first()
    }.memoize()
}
