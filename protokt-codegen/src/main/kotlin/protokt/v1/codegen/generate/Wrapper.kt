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
import protokt.v1.codegen.generate.WellKnownTypes.wrapWithWellKnownInterception
import protokt.v1.codegen.util.ConverterDetails
import protokt.v1.codegen.util.FieldType
import protokt.v1.codegen.util.StandardField
import kotlin.reflect.KFunction2

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception != null

    fun StandardField.wrapperRequiresNullability(ctx: Context) =
        withWrapper(ctx, ConverterDetails::requiresNullableProperty) ?: false

    private fun <T> StandardField.withWrapper(
        wrapOption: String?,
        ctx: Context,
        ifWrapped: (ConverterDetails) -> T
    ) =
        wrapOption?.let { wrap ->
            ifWrapped(
                converter(
                    protoTypeName.takeIf { it.isNotEmpty() }
                        ?.let { className }
                        // Protobuf primitives have no typeName
                        ?: requireNotNull(type.kotlinRepresentation) {
                            "no kotlin representation for type of $fieldName: $type"
                        }.asClassName(),
                    inferClassName(wrap, ctx.info.kotlinPackage),
                    ctx
                )
            )
        }

    private fun <R> StandardField.withWrapper(
        ctx: Context,
        ifWrapped: (ConverterDetails) -> R
    ) =
        withWrapper(wrapWithWellKnownInterception, ctx, ifWrapped)

    fun interceptSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx) {
            if (it.optimizedSizeof) {
                accessSize
            } else {
                interceptValueAccess(f, ctx, accessSize)
            }
        } ?: accessSize

    fun interceptFieldSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ) =
        f.withWrapper(ctx) {
            if (it.optimizedSizeof) {
                val sizeOf = OptimizedSizeOfConverter<Any, Any>::sizeOf
                callConverterMethod(sizeOf, it, accessSize, true)
            } else {
                f.sizeOf(accessSize)
            }
        } ?: f.sizeOf(accessSize)

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        accessValue: CodeBlock
    ): CodeBlock =
        f.withWrapper(ctx) {
            val unwrap = Converter<Any, Any>::unwrap
            callConverterMethod(unwrap, it, accessValue, true)
        } ?: accessValue

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.wrap(%L)", wrapName, arg)

    private fun callConverterMethod(
        methodName: KFunction2<*, *, *>,
        converterDetails: ConverterDetails,
        access: CodeBlock,
        nullChecked: Boolean
    ) =
        if (converterDetails.requiresNullableProperty && !nullChecked) {
            CodeBlock.of("%L?.run(%T::%L)", access, converterDetails.converterClassName, methodName.name)
        } else {
            CodeBlock.of("%T.%L(%L)", converterDetails.converterClassName, methodName.name, access)
        }

    fun wrapper(f: StandardField, ctx: Context) =
        f.withWrapper(ctx, ConverterDetails::converterClassName)

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
            f.withWrapper(ctx, ConverterDetails::kotlinClassName)
        }

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun <R> StandardField.withKeyWrap(
        ctx: Context,
        ifWrapped: (ConverterDetails) -> R
    ) =
        mapEntry!!.key.withWrapper(
            options.protokt.keyWrap.takeIf { it.isNotEmpty() },
            ctx,
            ifWrapped
        )

    fun interceptMapKeyTypeName(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, ConverterDetails::kotlinClassName)

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, ConverterDetails::converterClassName)

    private fun <R> StandardField.withValueWrap(
        ctx: Context,
        ifWrapped: (ConverterDetails) -> R
    ) =
        mapEntry!!.value.withWrapper(
            options.protokt.valueWrap.takeIf { it.isNotEmpty() },
            ctx,
            ifWrapped
        )

    fun interceptMapValueTypeName(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx, ConverterDetails::protoClassName)

    fun mapValueConverter(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx, ConverterDetails::converterClassName)

    private fun converter(protoClassName: ClassName, kotlinClassName: ClassName, ctx: Context) =
        ctx.info.context.classLookup.converter(protoClassName, kotlinClassName)
}
