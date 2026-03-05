/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.BytesSlice
import protokt.v1.Converter
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.util.GeneratorContext
import protokt.v1.codegen.util.StandardField
import protokt.v1.reflect.ClassLookup
import protokt.v1.reflect.ConverterDetails
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.WellKnownTypes.wrapWithWellKnownInterception
import protokt.v1.reflect.inferClassName
import kotlin.reflect.KFunction2

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception(options.wrap, protoTypeName) != null

    private fun <T> StandardField.withWrapper(
        wrap: String?,
        ctx: GeneratorContext,
        ifWrapped: (ConverterDetails) -> T
    ) =
        wrapWithWellKnownInterception(wrap, protoTypeName)?.let {
            ifWrapped(
                converter(
                    ClassLookup.evaluateProtobufTypeCanonicalName(
                        protoTypeName,
                        className.canonicalName,
                        type,
                        fieldName
                    ),
                    inferClassName(it, ctx.kotlinPackage)
                        .let { (pkg, names) -> ClassName(pkg, names).canonicalName },
                    ctx
                )
            )
        }

    private fun <R> StandardField.withWrapper(
        ctx: GeneratorContext,
        ifWrapped: (ConverterDetails) -> R
    ) =
        withWrapper(options.wrap, ctx, ifWrapped)

    fun interceptSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx.info.context) {
            interceptValueAccess(f, ctx, accessSize)
        } ?: accessSize

    fun interceptFieldSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        fieldAccess: CodeBlock,
        ctx: Context
    ) =
        accessSize

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        accessValue: CodeBlock
    ): CodeBlock =
        f.withWrapper(ctx.info.context) {
            callConverterMethod(Converter<Any, Any>::unwrap, it, accessValue)
        } ?: accessValue

    private fun callConverterMethod(
        method: KFunction2<*, *, *>,
        converterDetails: ConverterDetails,
        access: CodeBlock,
    ) =
        CodeBlock.of("%T.%L(%L)", converterDetails.converter::class.asClassName(), method.name, access)

    fun wrapper(f: StandardField, ctx: Context) =
        f.withWrapper(ctx.info.context, ::converterClassName)

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
            if (f.type == FieldType.Message && !f.repeated) {
                defaultValue
            } else {
                wrapField(f, ctx, defaultValue) ?: defaultValue
            }
        }

    fun wrapField(f: StandardField, ctx: Context, argToConverter: CodeBlock) =
        wrapper(f, ctx)?.let { wrapField(it, argToConverter) }

    private fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.%L(%L)", wrapName, Converter<Any, Any>::wrap.name, arg)

    fun StandardField.interceptTypeName(ctx: Context) =
        if (bytesSlice) {
            BytesSlice::class.asTypeName()
        } else {
            withWrapper(ctx.info.context, ::kotlinClassName)
        } ?: className

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun kotlinClassName(converterDetails: ConverterDetails) =
        ClassName.bestGuess(converterDetails.kotlinCanonicalClassName)

    private fun converterClassName(converterDetails: ConverterDetails) =
        converterDetails.converter::class.asClassName()

    fun StandardField.cachingFieldInfo(ctx: Context, mapEntry: Boolean): CachingFieldInfo? {
        if (repeated || isMap || mapEntry) return null
        if (!wrapped) {
            return if (type == FieldType.String) CachingFieldInfo.PlainString(optional) else null
        }
        val nullable = type == FieldType.Message || optional
        return withWrapper(ctx.info.context) { details ->
            CachingFieldInfo.Converted(
                details.converter::class.asClassName(),
                details.converter.wrapped.asTypeName(),
                type,
                nullable
            )
        }
    }

    private fun converter(protoClassName: String, kotlinClassName: String, ctx: GeneratorContext) =
        ctx.classLookup.converter(protoClassName, kotlinClassName)
}
