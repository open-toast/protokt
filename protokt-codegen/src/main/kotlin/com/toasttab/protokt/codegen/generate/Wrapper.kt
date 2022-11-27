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

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
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
import com.toasttab.protokt.codegen.util.emptyToNone
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.BytesSlice
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception.isDefined()

    private val StandardField.keyWrap
        get() = options.protokt.keyWrap.emptyToNone()

    private val StandardField.valueWrap
        get() = options.protokt.valueWrap.emptyToNone()

    private fun <R> StandardField.foldWrap(
        wrap: Option<String>,
        pkg: String,
        ctx: GeneratorContext,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        wrap.fold(
            ifEmpty
        ) {
            ifSome(
                getClass(inferClassName(it, pkg), ctx),
                protoTypeName.emptyToNone().fold(
                    {
                        // Protobuf primitives have no typeName
                        requireNotNull(type.kotlinRepresentation) {
                            "no kotlin representation for type of " +
                                "$fieldName: $type"
                        }
                    },
                    { getClass(className, ctx) }
                )
            )
        }

    private fun <R> StandardField.foldFieldWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        foldWrap(
            wrapWithWellKnownInterception,
            ctx.info.kotlinPackage,
            ctx.info.context,
            ifEmpty,
            ifSome
        )

    fun interceptSizeof(
        f: StandardField,
        s: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.foldFieldWrap(
            ctx,
            { interceptValueAccess(f, ctx, s) },
            { wrapper, wrapped ->
                if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                    s
                } else {
                    interceptValueAccess(f, ctx, s)
                }
            }
        )

    fun interceptFieldSizeof(
        f: StandardField,
        s: CodeBlock,
        ctx: Context
    ) =
        f.foldFieldWrap(
            ctx,
            { CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s)) },
            { wrapper, wrapped ->
                if (converter(wrapper, wrapped, ctx) is OptimizedSizeofConverter<*, *>) {
                    CodeBlock.of("%T.sizeof(%L)", unqualifiedConverterWrap(wrapper, wrapped, ctx), s)
                } else {
                    CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s))
                }
            }
        )

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        s: CodeBlock = CodeBlock.of("%N", f.fieldName)
    ): CodeBlock =
        f.foldFieldWrap(
            ctx,
            { s },
            { wrapper, wrapped ->
                CodeBlock.of(
                    "%T.unwrap(%L)",
                    unqualifiedConverterWrap(wrapper, wrapped, ctx),
                    s
                )
            }
        )

    private fun interceptDeserializedValue(f: StandardField, s: CodeBlock, ctx: Context) =
        wrapperName(f, ctx).fold(
            { s },
            { wrapField(it, s) }
        )

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.wrap(%L)", wrapName, arg)

    fun wrapperName(f: StandardField, ctx: Context) =
        f.foldFieldWrap(
            ctx,
            { None },
            { wrapper, wrapped ->
                Some(unqualifiedConverterWrap(wrapper, wrapped, ctx))
            }
        )

    fun interceptReadFn(f: StandardField, s: CodeBlock) =
        f.foldBytesSlice(
            { s },
            { CodeBlock.of("readBytesSlice()") }
        )

    fun interceptDefaultValue(f: StandardField, s: CodeBlock, ctx: Context) =
        f.foldBytesSlice(
            {
                f.foldSingularMessage(
                    { interceptDeserializedValue(f, s, ctx) },
                    { s }
                )
            },
            { CodeBlock.of("%T.empty()", BytesSlice::class) }
        )

    fun interceptTypeName(f: StandardField, t: TypeName, ctx: Context): TypeName =
        f.foldBytesSlice(
            { f.foldFieldWrap(ctx, { t }, unqualifiedWrap()) },
            { BytesSlice::class.asTypeName() }
        )

    private fun <R> StandardField.foldBytesSlice(
        ifNotSlice: () -> R,
        ifSlice: () -> R
    ) =
        if (options.protokt.bytesSlice) {
            ifSlice()
        } else {
            ifNotSlice()
        }

    private fun <R> StandardField.foldSingularMessage(
        ifNotSingularMessage: () -> R,
        ifSingularMessage: () -> R
    ) =
        if (type == FieldType.MESSAGE && !repeated) {
            ifSingularMessage()
        } else {
            ifNotSingularMessage()
        }

    private fun <R> StandardField.foldKeyWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.key
            ?.foldWrap(keyWrap, ctx.info.kotlinPackage, ctx.info.context, ifEmpty, ifSome)

    fun interceptMapKeyTypeName(f: StandardField, t: TypeName, ctx: Context) =
        f.foldKeyWrap(ctx, { t }, unqualifiedWrap())

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.foldKeyWrap(
            ctx,
            { null },
            { wrapper, wrapped -> converter(wrapper, wrapped, ctx)::class.asTypeName() }
        )

    private fun <R> StandardField.foldValueWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.value
            ?.foldWrap(valueWrap, ctx.info.kotlinPackage, ctx.info.context, ifEmpty, ifSome)

    fun interceptMapValueTypeName(f: StandardField, t: TypeName, ctx: Context) =
        f.foldValueWrap(ctx, { t }, unqualifiedWrap())

    fun mapValueConverter(f: StandardField, ctx: Context) =
        f.foldValueWrap(
            ctx,
            { null },
            { wrapper, wrapped ->
                unqualifiedConverterWrap(wrapper, wrapped, ctx)
            }
        )

    private fun unqualifiedConverterWrap(
        wrapper: KClass<*>,
        wrapped: KClass<*>,
        ctx: Context
    ) =
        converter(wrapper, wrapped, ctx)::class.asTypeName()

    private fun unqualifiedWrap() =
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
