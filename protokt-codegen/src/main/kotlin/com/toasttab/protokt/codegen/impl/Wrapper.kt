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

import arrow.syntax.function.memoize
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.codegen.impl.ClassLookup.converters
import com.toasttab.protokt.codegen.impl.ClassLookup.getClass
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.model.PPackage
import com.toasttab.protokt.codegen.model.possiblyQualify
import com.toasttab.protokt.ext.Converter
import com.toasttab.protokt.ext.OptimizedSizeofConverter
import com.toasttab.protokt.rt.PType

internal object Wrapper {
    val converterPkg = PPackage.fromString(Converter::class.java.`package`.name)

    val StandardField.wrapped
        get() = options.protokt.wrap.isNotEmpty()

    private fun <R> StandardField.foldWrap(
        ctx: Context,
        ifEmpty: () -> R,
        ifSome: (PClass) -> R
    ) =
        options.protokt.wrap
            .emptyToNone()
            .map { PClass.fromName(it).possiblyQualify(ctx.pkg) }
            .fold(ifEmpty, ifSome)

    fun interceptSizeof(
        f: StandardField,
        s: String,
        ctx: Context
    ) =
        f.foldWrap(
            ctx,
            { interceptValueAccess(f, ctx, s) },
            {
                if (converter(it, ctx) is OptimizedSizeofConverter<*, *>) {
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
                FieldSizeOfRF.render(
                    FieldSizeOfVar to f,
                    NameSizeOfVar to s
                )
            },
            {
                if (converter(it, ctx) is OptimizedSizeofConverter<*, *>) {
                    ConcatWithScopeRF.render(
                        ScopedValueRenderVar to
                            ScopedValueSt(
                                unqualifiedWrap(converterClass(it, ctx), ctx),
                                SizeofOptionRF.render(ArgVar to s)
                            )
                    )
                } else {
                    FieldSizeOfRF.render(
                        FieldSizeOfVar to f,
                        NameSizeOfVar to s
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
            {
                AccessFieldRF.render(
                    WrapNameVar to
                        PClass.fromClass(converter(it, ctx)::class)
                            .unqualify(ctx.pkg),
                    ArgVar to s
                )
            }
        )

    private fun unqualifiedWrap(wrap: PClass, ctx: Context) =
        if (wrap.isInPackage(ctx.pkg)) {
            wrap.nestedName
        } else {
            wrap.unqualify(converterPkg)
        }

    private fun converterClass(wrapper: PClass, ctx: Context) =
        PClass.fromClass(converter(wrapper, ctx)::class)

    private val converter = { wrapper: PClass, ctx: Context ->
        converters(ctx.desc.params.classpath).find {
            it.wrapper == getClass(wrapper, ctx.desc.params)
        } ?: throw Exception(
            "${ctx.desc.name}: No converter found for wrapper type " +
                wrapper.qualifiedName
        )
    }.memoize()

    fun interceptDeserializedValue(f: StandardField, s: String, ctx: Context) =
        f.foldWrap(
            ctx,
            { s },
            {
                WrapFieldRF.render(
                    WrapNameVar to unqualifiedWrap(converterClass(it, ctx), ctx),
                    ArgVar to s,
                    TypeOptionVar to f.type,
                    OneofOptionVar to true
                )
            }
        )

    fun interceptReadFn(f: StandardField, s: String) =
        f.foldBytesSlice(
            { s },
            { ReadBytesSliceRF.render() }
        )

    fun interceptDefaultValue(f: StandardField, s: String, ctx: Context) =
        f.foldBytesSlice(
            {
                f.foldSingularMessage(
                    { interceptDeserializedValue(f, s, ctx) },
                    { s }
                )
            },
            { DefaultBytesSliceRF.render() }
        )

    fun interceptTypeName(f: StandardField, t: String, ctx: Context) =
        f.foldBytesSlice(
            {
                f.foldWrap(
                    ctx,
                    { t },
                    { unqualifiedWrap(it, ctx) }
                )
            },
            { BytesSliceRF.render() }
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
