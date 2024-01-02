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
import protokt.v1.OptimizedSizeOfConverter
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Nullability.hasNonNullOption
import protokt.v1.codegen.generate.WellKnownTypes.wrapWithWellKnownInterception
import protokt.v1.codegen.util.GeneratorContext
import protokt.v1.codegen.util.StandardField
import protokt.v1.reflect.ClassLookup
import protokt.v1.reflect.ConverterDetails
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.inferClassName
import kotlin.reflect.KFunction2

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception != null

    fun StandardField.wrapperRequiresNullability(ctx: Context) =
        wrapperRequiresNonNullOptionForNonNullity(ctx.info.context) && !hasNonNullOption

    fun StandardField.wrapperRequiresNonNullOptionForNonNullity(ctx: GeneratorContext) =
        withWrapper(ctx) { it.cannotDeserializeDefaultValue && !repeated } ?: false

    private fun <T> StandardField.withWrapper(
        wrapOption: String?,
        ctx: GeneratorContext,
        ifWrapped: (ConverterDetails) -> T
    ) =
        wrapOption?.let { wrap ->
            ifWrapped(
                converter(
                    ClassLookup.evaluateProtobufTypeCanonicalName(
                        protoTypeName,
                        className.canonicalName,
                        type,
                        fieldName
                    ),
                    inferClassName(wrap, ctx.kotlinPackage)
                        .let { (pkg, names) -> ClassName(pkg, names).canonicalName },
                    ctx
                )
            )
        }

    private fun <R> StandardField.withWrapper(
        ctx: GeneratorContext,
        ifWrapped: (ConverterDetails) -> R
    ) =
        withWrapper(wrapWithWellKnownInterception, ctx, ifWrapped)

    fun interceptSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx.info.context) {
            if (it.optimizedSizeof) {
                accessSize
            } else {
                interceptValueAccess(f, ctx, accessSize)
            }
        } ?: accessSize

    fun interceptFieldSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        fieldAccess: CodeBlock,
        ctx: Context
    ) =
        f.withWrapper(ctx.info.context) {
            if (it.optimizedSizeof) {
                callConverterMethod(OptimizedSizeOfConverter<Any, Any>::sizeOf, it, fieldAccess)
            } else {
                accessSize
            }
        } ?: accessSize

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        accessValue: CodeBlock
    ): CodeBlock =
        f.withWrapper(ctx.info.context) {
            callConverterMethod(Converter<Any, Any>::unwrap, it, accessValue)
        } ?: accessValue

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.%L(%L)", wrapName, Converter<Any, Any>::wrap.name, arg)

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
                wrapper(f, ctx)?.let { wrapField(it, defaultValue) } ?: defaultValue
            }
        }

    fun interceptTypeName(f: StandardField, ctx: Context) =
        if (f.bytesSlice) {
            BytesSlice::class.asTypeName()
        } else {
            f.withWrapper(ctx.info.context, ::kotlinClassName)
        }

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun <R> StandardField.withKeyWrap(
        ctx: Context,
        ifWrapped: (ConverterDetails) -> R
    ) =
        mapEntry!!.key.withWrapper(
            options.protokt.keyWrap.takeIf { it.isNotEmpty() },
            ctx.info.context,
            ifWrapped
        )

    fun interceptMapKeyTypeName(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, ::kotlinClassName)

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, ::converterClassName)

    fun interceptMapValueTypeName(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx, ::kotlinClassName)

    fun mapValueConverter(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx, ::converterClassName)

    private fun kotlinClassName(converterDetails: ConverterDetails) =
        ClassName.bestGuess(converterDetails.kotlinCanonicalClassName)

    private fun converterClassName(converterDetails: ConverterDetails) =
        converterDetails.converter::class.asClassName()

    private fun <R> StandardField.withValueWrap(
        ctx: Context,
        ifWrapped: (ConverterDetails) -> R
    ) =
        mapEntry!!.value.withWrapper(
            options.protokt.valueWrap.takeIf { it.isNotEmpty() },
            ctx.info.context,
            ifWrapped
        )

    private fun converter(protoClassName: String, kotlinClassName: String, ctx: GeneratorContext) =
        ctx.classLookup.converter(protoClassName, kotlinClassName)
}
