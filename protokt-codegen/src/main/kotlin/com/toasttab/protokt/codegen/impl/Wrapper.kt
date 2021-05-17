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
import com.toasttab.protokt.codegen.impl.ClassLookup.converters
import com.toasttab.protokt.codegen.impl.ClassLookup.getClass
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.WellKnownTypes.wrapWithWellKnownInterception
import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Options.AccessField
import com.toasttab.protokt.codegen.template.Options.BytesSlice
import com.toasttab.protokt.codegen.template.Options.DefaultBytesSlice
import com.toasttab.protokt.codegen.template.Options.ReadBytesSlice
import com.toasttab.protokt.codegen.template.Options.Sizeof
import com.toasttab.protokt.codegen.template.Options.WrapField
import com.toasttab.protokt.codegen.template.Renderers.ConcatWithScope
import com.toasttab.protokt.codegen.template.Renderers.FieldSizeof
import com.toasttab.protokt.ext.OptimizedSizeofConverter
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
                                    "$name: $type"
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
            ctx.pkg,
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
                FieldSizeof.render(
                    field = f,
                    name = s
                )
            },
            { wrapper, wrapped ->
                if (
                    converter(wrapper, wrapped, ctx) is
                        OptimizedSizeofConverter<*, *>
                ) {
                    ConcatWithScope.render(
                        scope = unqualifiedConverterWrap(wrapper, wrapped, ctx),
                        value = Sizeof.render(arg = s)
                    )
                } else {
                    FieldSizeof.render(
                        field = f,
                        name = s
                    )
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
                AccessField.render(
                    wrapName = unqualifiedConverterWrap(wrapper, wrapped, ctx),
                    arg = s
                )
            }
        )

    private fun interceptDeserializedValue(
        f: StandardField,
        s: String,
        ctx: Context
    ) =
        wrapperName(f, ctx).fold(
            { s },
            {
                WrapField.render(
                    wrapName = it,
                    arg = s,
                    type = f.type,
                    oneof = true
                )
            }
        )

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
            { ReadBytesSlice.render() }
        )

    fun interceptDefaultValue(f: StandardField, s: String, ctx: Context) =
        f.foldBytesSlice(
            {
                f.foldSingularMessage(
                    { interceptDeserializedValue(f, s, ctx) },
                    { s }
                )
            },
            { DefaultBytesSlice.render() }
        )

    fun interceptTypeName(f: StandardField, t: String, ctx: Context) =
        f.foldBytesSlice(
            { f.foldFieldWrap(ctx, { t }, unqualifiedWrap(ctx)) },
            { BytesSlice.render() }
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
            ?.foldWrap(keyWrap, ctx.pkg, ctx.desc.context, ifEmpty, ifSome)

    fun interceptMapKeyTypeName(f: StandardField, t: String, ctx: Context) =
        f.foldKeyWrap(ctx, { t }, unqualifiedWrap(ctx))

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.foldKeyWrap(
            ctx,
            { null },
            { wrapper, wrapped ->
                unqualifiedWrap(
                    converter(wrapper, wrapped, ctx)::class,
                    ctx.pkg
                )
            }
        )

    private fun <R> StandardField.foldValueWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        mapEntry?.value
            ?.foldWrap(valueWrap, ctx.pkg, ctx.desc.context, ifEmpty, ifSome)

    fun interceptMapValueTypeName(f: StandardField, t: String, ctx: Context) =
        f.foldValueWrap(ctx, { t }, unqualifiedWrap(ctx))

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
        unqualifiedWrap(
            converter(wrapper, wrapped, ctx)::class,
            ctx.pkg
        )

    private fun unqualifiedWrap(ctx: Context) =
        fun(wrapper: KClass<*>, _: Any) =
            unqualifiedWrap(wrapper, ctx.pkg)

    private fun unqualifiedWrap(wrap: KClass<*>, pkg: PPackage) =
        PClass.fromClass(wrap).renderName(pkg)

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
