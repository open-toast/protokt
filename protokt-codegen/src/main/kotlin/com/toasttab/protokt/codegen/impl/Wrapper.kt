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
import arrow.core.Some
import arrow.syntax.function.memoize
import com.toasttab.protokt.codegen.PluginContext
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.ClassLookup.converters
import com.toasttab.protokt.codegen.impl.ClassLookup.getClass
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.impl.WellKnownTypes.wrapWithWellKnownInterception
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.codegen.template.Options.AccessField
import com.toasttab.protokt.codegen.template.Options.BytesSlice
import com.toasttab.protokt.codegen.template.Options.DefaultBytesSlice
import com.toasttab.protokt.codegen.template.Options.ReadBytesSlice
import com.toasttab.protokt.codegen.template.Options.Sizeof
import com.toasttab.protokt.codegen.template.Options.WrapField
import com.toasttab.protokt.codegen.template.Renderers.ConcatWithScope
import com.toasttab.protokt.codegen.template.Renderers.FieldSizeof
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.PType
import kotlin.reflect.KClass

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception.isDefined()

    private fun <R> StandardField.foldWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        foldWrap(ctx.pkg, ctx.desc.context, ifEmpty, ifSome)

    fun <R> StandardField.foldWrap(
        pkg: PPackage,
        ctx: PluginContext,
        ifEmpty: () -> R,
        ifSome: (wrapper: KClass<*>, wrapped: KClass<*>) -> R
    ) =
        wrapWithWellKnownInterception
            .map {
                getClass(
                    PClass.fromName(it).possiblyQualify(pkg),
                    ctx
                )
            }
            .fold(
                ifEmpty,
                {
                    ifSome(
                        it,
                        protoTypeName.emptyToNone().fold(
                            {
                                // Protobuf primitives have no typeName
                                requireNotNull(type.kotlinRepresentation) {
                                    "no kotlin representation for type of " +
                                        "$name: $type"
                                }
                            },
                            { getClass(typePClass(ctx), ctx) }
                        )
                    )
                }
            )

    fun interceptSizeof(
        f: StandardField,
        s: String,
        ctx: Context
    ) =
        f.foldWrap(
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
        f.foldWrap(
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
                        scope =
                            unqualifiedWrap(
                                converterClass(wrapper, wrapped, ctx),
                                ctx.pkg
                            ),
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
        f.foldWrap(
            ctx,
            { s },
            { wrapper, wrapped ->
                AccessField.render(
                    wrapName =
                        PClass.fromClass(
                            converter(wrapper, wrapped, ctx)::class
                        ).renderName(ctx.pkg),
                    arg = s
                )
            }
        )

    private fun unqualifiedWrap(wrap: KClass<*>, pkg: PPackage) =
        PClass.fromClass(wrap).renderName(pkg)

    private fun converterClass(wrapper: KClass<*>, wrapped: KClass<*>, ctx: Context) =
        converter(wrapper, wrapped, ctx)::class

    private fun converter(wrapper: KClass<*>, wrapped: KClass<*>, ctx: Context) =
        converter(wrapper, wrapped, ctx.desc.context)

    val converter = { wrapper: KClass<*>, wrapped: KClass<*>, ctx: PluginContext ->
        converters(ctx.classpath).find {
            it.wrapper == wrapper && it.wrapped == wrapped
        } ?: throw Exception(
            "${ctx.fileName}: No converter found for wrapper type " +
                "${wrapper.qualifiedName} from type ${wrapped.qualifiedName}"
        )
    }.memoize()

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
        f.foldWrap(
            ctx,
            { None },
            { wrapper, wrapped ->
                Some(
                    unqualifiedWrap(
                        converterClass(wrapper, wrapped, ctx), ctx.pkg
                    )
                )
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
            {
                f.foldWrap(
                    ctx,
                    { t },
                    { wrapper, _ -> unqualifiedWrap(wrapper, ctx.pkg) }
                )
            },
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
        if (type == PType.MESSAGE && !repeated) {
            ifSingularMessage()
        } else {
            ifNotSingularMessage()
        }
}
