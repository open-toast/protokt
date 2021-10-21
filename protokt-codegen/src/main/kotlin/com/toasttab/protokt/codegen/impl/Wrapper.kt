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

package com.toasttab.protokt.codegen.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.memoize
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.toasttab.protokt.codegen.annotators.Annotator.Context
import com.toasttab.protokt.codegen.annotators.box
import com.toasttab.protokt.codegen.impl.ClassLookup.converters
import com.toasttab.protokt.codegen.impl.ClassLookup.getClass
import com.toasttab.protokt.codegen.impl.WellKnownTypes.wrapWithWellKnownInterception
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.BytesSlice
import kotlin.reflect.KClass

object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception.isDefined()

    val StandardField.keyWrap
        get() = options.protokt.keyWrap.emptyToNone()

    val StandardField.keyWrapped
        get() = keyWrap.isDefined()

    val StandardField.valueWrap
        get() = options.protokt.valueWrap.emptyToNone()

    val StandardField.valueWrapped
        get() = valueWrap.isDefined()

    fun <R> StandardField.foldWrap(
        wrap: Option<String>,
        pkg: PPackage,
        ctx: ProtocolContext,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        wrap.fold(
            ifEmpty,
            {
                ifSome(
                    getClass(PClass.fromName(it).possiblyQualify(pkg), ctx),
                    protoTypeName.emptyToNone().fold(
                        {
                            // Protobuf primitives have no typeName
                            requireNotNull(type.kotlinRepresentation) {
                                "no kotlin representation for type of " +
                                    "$fieldName: $type"
                            }
                        },
                        { getClass(typePClass, ctx) }
                    )
                )
            }
        )

    private fun <R> StandardField.foldFieldWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        foldWrap(
            wrapWithWellKnownInterception,
            ctx.desc.kotlinPackage,
            ctx.desc.context,
            ifEmpty,
            ifSome
        )

    fun interceptSizeof(
        f: StandardField,
        s: String,
        ctx: Context
    ) =
        f.foldFieldWrap(
            ctx,
            { interceptValueAccess(f, ctx, s) },
            { wrapper, wrapped ->
                if (
                    converter(wrapper, wrapped, ctx) is
                    OptimizedSizeofConverter<*, *>
                ) {
                    s
                } else {
                    interceptValueAccess(f, ctx, s)
                }
            }
        )

    fun interceptFieldSizeof(
        f: StandardField,
        s: String,
        ctx: Context
    ) =
        f.foldFieldWrap(
            ctx,
            {
                CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s))
            },
            { wrapper, wrapped ->
                if (
                    converter(wrapper, wrapped, ctx) is
                    OptimizedSizeofConverter<*, *>
                ) {
                    CodeBlock.of("%T.sizeof(%L)", unqualifiedConverterWrap(wrapper, wrapped, ctx), s)
                } else {
                    CodeBlock.of("%M(%L)", runtimeFunction("sizeof"), f.box(s))
                }
            }
        )

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        s: String = f.fieldName
    ) =
        f.foldFieldWrap(
            ctx,
            { s },
            { wrapper, wrapped ->
                "${unqualifiedConverterWrap(wrapper, wrapped, ctx)}.unwrap($s)"
            }
        )

    private fun interceptDeserializedValue(
        f: StandardField,
        s: CodeBlock,
        ctx: Context
    ) =
        wrapperName(f, ctx).fold(
            { s },
            {
                wrapField(
                    wrapName = it.toString(),
                    arg = s,
                    f = f.type,
                    oneof = true
                )
            }
        )

    fun wrapField(wrapName: String, arg: CodeBlock, f: FieldType?, oneof: Boolean) = when {
        f == FieldType.BYTES -> CodeBlock.of("%L.wrap(%L.bytes)", wrapName, arg)
        f == FieldType.MESSAGE && !oneof -> CodeBlock.of("%L.wrap(%L!!)", wrapName, arg)
        else -> CodeBlock.of("%L.wrap(%L)", wrapName, arg)
    }

    fun wrapperName(f: StandardField, ctx: Context) =
        f.foldFieldWrap(
            ctx,
            { None },
            { wrapper, wrapped ->
                Some(unqualifiedConverterWrap(wrapper, wrapped, ctx))
            }
        )

    fun interceptReadFn(f: StandardField, s: String) =
        f.foldBytesSlice(
            { s },
            { "readBytesSlice()" }
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
            ?.foldWrap(keyWrap, ctx.desc.kotlinPackage, ctx.desc.context, ifEmpty, ifSome)

    fun interceptMapKeyTypeName(f: StandardField, t: TypeName, ctx: Context) =
        f.foldKeyWrap(ctx, { t }, unqualifiedWrap())

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.foldKeyWrap(
            ctx,
            { null },
            { wrapper, wrapped -> unqualifiedWrap(converter(wrapper, wrapped, ctx)::class) }
        )

    private fun <R> StandardField.foldValueWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.value
            ?.foldWrap(valueWrap, ctx.desc.kotlinPackage, ctx.desc.context, ifEmpty, ifSome)

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
        unqualifiedWrap(converter(wrapper, wrapped, ctx)::class)

    private fun unqualifiedWrap() =
        fun(wrapper: KClass<*>, _: Any) =
            unqualifiedWrap(wrapper)

    private fun unqualifiedWrap(wrap: KClass<*>) =
        PClass.fromClass(wrap).toTypeName()

    private fun converter(wrapper: KClass<*>, wrapped: KClass<*>, ctx: Context) =
        converter(wrapper, wrapped, ctx.desc.context)

    val converter = { wrapper: KClass<*>, wrapped: KClass<*>, ctx: ProtocolContext ->
        converters(ctx.classpath).find {
            it.wrapper == wrapper && it.wrapped == wrapped
        } ?: error(
            "${ctx.fileName}: No converter found for wrapper type " +
                "${wrapper.qualifiedName} from type ${wrapped.qualifiedName}"
        )
    }.memoize()
}
