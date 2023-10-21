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
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.WellKnownTypes.wrapWithWellKnownInterception
import protokt.v1.codegen.util.FieldType
import protokt.v1.codegen.util.StandardField

internal object Wrapper {
    val StandardField.wrapped
        get() = wrapWithWellKnownInterception != null

    fun StandardField.wrapperRequiresNullability(ctx: Context) =
        withWrapper(ctx) { protoClass, kotlinClass ->
            converter(protoClass, kotlinClass, ctx).requiresNullableProperty
        } ?: false

    private fun <T> StandardField.withWrapper(
        wrapOption: String?,
        pkg: String,
        ifWrapped: (protoClass: ClassName, kotlinClass: ClassName) -> T
    ) =
        wrapOption?.let { wrap ->
            ifWrapped(
                protoTypeName.takeIf { it.isNotEmpty() }
                    ?.let { className }
                    // Protobuf primitives have no typeName
                    ?: requireNotNull(type.kotlinRepresentation) {
                        "no kotlin representation for type of $fieldName: $type"
                    }.asClassName(),
                inferClassName(wrap, pkg)
            )
        }

    private fun <R> StandardField.withWrapper(
        ctx: Context,
        ifWrapped: (protoClass: ClassName, kotlinClass: ClassName) -> R
    ) =
        withWrapper(
            wrapWithWellKnownInterception,
            ctx.info.kotlinPackage,
            ifWrapped
        )

    fun interceptSizeof(
        f: StandardField,
        accessSize: CodeBlock,
        ctx: Context
    ): CodeBlock =
        f.withWrapper(ctx) { protoClass, kotlinClass ->
            val converter = converter(protoClass, kotlinClass, ctx)
            if (converter.optimizedSizeof) {
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
        f.withWrapper(ctx) { protoClass, kotlinClass ->
            val converter = converter(protoClass, kotlinClass, ctx)
            if (converter.optimizedSizeof) {
                CodeBlock.of("%T.sizeOf(%L)", converter.className, accessSize)
            } else {
                f.sizeOf(accessSize)
            }
        } ?: f.sizeOf(accessSize)

    fun interceptValueAccess(
        f: StandardField,
        ctx: Context,
        accessValue: CodeBlock
    ): CodeBlock =
        f.withWrapper(ctx) { protoClass, kotlinClass ->
            CodeBlock.of(
                "%T.unwrap(%L)",
                converter(protoClass, kotlinClass, ctx).className,
                accessValue
            )
        } ?: accessValue

    fun wrapField(wrapName: TypeName, arg: CodeBlock) =
        CodeBlock.of("%T.wrap(%L)", wrapName, arg)

    fun wrapper(f: StandardField, ctx: Context) =
        f.withWrapper(ctx) { protoClass, kotlinClass ->
            converter(protoClass, kotlinClass, ctx).className
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
            f.withWrapper(ctx, kotlinTypeName())
        }

    private val StandardField.bytesSlice
        get() = options.protokt.bytesSlice

    private fun <R> StandardField.withKeyWrap(
        ctx: Context,
        ifWrapped: (protoClass: ClassName, kotlinClass: ClassName) -> R
    ) =
        mapEntry!!.key.withWrapper(
            options.protokt.keyWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ifWrapped
        )

    fun interceptMapKeyTypeName(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx, kotlinTypeName())

    fun mapKeyConverter(f: StandardField, ctx: Context) =
        f.withKeyWrap(ctx) { protoClass, kotlinClass ->
            converter(protoClass, kotlinClass, ctx).className
        }

    private fun <R> StandardField.withValueWrap(
        ctx: Context,
        ifWrapped: (wrapper: ClassName, wrapped: ClassName) -> R
    ) =
        mapEntry!!.value.withWrapper(
            options.protokt.valueWrap.takeIf { it.isNotEmpty() },
            ctx.info.kotlinPackage,
            ifWrapped
        )

    fun interceptMapValueTypeName(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx) { protoClass, _ -> protoClass }

    fun mapValueConverter(f: StandardField, ctx: Context) =
        f.withValueWrap(ctx) { protoClass, kotlinClass ->
            converter(protoClass, kotlinClass, ctx).className
        }

    private fun kotlinTypeName() =
        { _: Any, kotlinClass: ClassName -> kotlinClass }

    private fun converter(protoClass: ClassName, kotlinClass: ClassName, ctx: Context) =
        ctx.info.context.classLookup.converter(protoClass, kotlinClass)
}
