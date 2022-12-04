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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.generate.WellKnownTypes.wrapWithWellKnownInterception
import com.toasttab.protokt.codegen.util.FieldType
import com.toasttab.protokt.codegen.util.GeneratorContext
import com.toasttab.protokt.codegen.util.StandardField
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.BytesSlice
import kotlin.reflect.KClass

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
                ctx.classLookup.getClass(inferClassName(wrap, pkg)),
                protoTypeName.takeIf { it.isNotEmpty() }
                    ?.let { ctx.classLookup.getClass(className) }
                    // Protobuf primitives have no typeName
                    ?: requireNotNull(type.kotlinRepresentation) {
                        "no kotlin representation for type of $fieldName: $type"
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
        accessSize: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx) { wrapper, wrapped ->
            if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                accessSize
            } else {
                interceptValueAccess(f, ctx, accessSize)
            }
        } ?: interceptValueAccess(f, ctx, accessSize)

    fun interceptFieldSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ) =
        f.withWrapper(ctx) { wrapper, wrapped ->
            if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                CodeBlock.of("%T.sizeof(%L)", converterTypeName(wrapper, wrapped, ctx), accessSize)
            } else {
                CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(accessSize))
            }
        } ?: CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(accessSize))

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        accessValue: CodeBlock
    ): CodeBlock =
        f.withWrapper(ctx) { wrapper, wrapped ->
            CodeBlock.of(
                "%T.unwrap(%L)",
                converterTypeName(wrapper, wrapped, ctx),
                accessValue
            )
        } ?: accessValue

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.wrap(%L)", wrapName, arg)

    fun wrapper(f: StandardField, ctx: Context) =
        f.withWrapper(ctx) { wrapper, wrapped ->
            converterTypeName(wrapper, wrapped, ctx)
        }

    fun interceptRead(f: StandardField, readFunction: CodeBlock) =
        if (f.bytesSlice) {
            CodeBlock.of("readBytesSlice()")
        } else {
            readFunction
        }

    fun interceptDefaultValue(f: StandardField, defaultValue: CodeBlock, ctx: Context) =
        if (f.bytesSlice) {
            CodeBlock.of("%T.empty()", BytesSlice::class)
        } else {
            if (f.type == FieldType.MESSAGE && !f.repeated) {
                defaultValue
            } else {
                wrapper(f, ctx)?.let { wrapField(it, defaultValue) } ?: defaultValue
            }
        }

    fun interceptTypeName(f: StandardField, ctx: Context) =
        if (f.bytesSlice) {
            BytesSlice::class.asTypeName()
        } else {
            f.withWrapper(ctx, wrapperTypeName())
        }

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun <R> StandardField.withKeyWrap(
        ctx: Context,
        ifWrapped: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry!!.key.withWrapper(
            options.protokt.keyWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifWrapped
        )

    fun interceptMapKeyTypeName(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, wrapperTypeName())

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx) { wrapper, wrapped ->
            converter(wrapper, wrapped, ctx)::class.asTypeName()
        }

    private fun <R> StandardField.withValueWrap(
        ctx: Context,
        ifWrapped: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry!!.value.withWrapper(
            options.protokt.valueWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifWrapped
        )

    fun interceptMapValueTypeName(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx, wrapperTypeName())

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
        ctx.info.context.classLookup.converter(wrapper, wrapped)
}
