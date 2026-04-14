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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.Bytes
import protokt.v1.LazyConvertingList
import protokt.v1.LazyConvertingMap
import protokt.v1.codegen.generate.CodeGenerator.Context
import protokt.v1.codegen.generate.Wrapper.interceptFieldSizeof
import protokt.v1.codegen.generate.Wrapper.interceptSizeof
import protokt.v1.codegen.generate.Wrapper.interceptTypeName
import protokt.v1.codegen.generate.Wrapper.interceptValueAccess
import protokt.v1.codegen.generate.Wrapper.wrapped
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.Oneof
import protokt.v1.codegen.util.SizeFn
import protokt.v1.codegen.util.StandardField
import protokt.v1.codegen.util.sizeFn
import protokt.v1.reflect.FieldType

internal const val SERIALIZED_SIZE = "__serializedSize"

internal fun generateMessageSize(
    msg: Message,
    properties: List<PropertySpec>,
    ctx: Context,
    propertyInfoList: List<PropertyInfo> = emptyList()
) =
    MessageSizeGenerator(msg, properties, ctx, propertyInfoList).generate()

private class MessageSizeGenerator(
    private val msg: Message,
    private val properties: List<PropertySpec>,
    private val ctx: Context,
    private val propertyInfoList: List<PropertyInfo>
) {
    private val resultVarName =
        run {
            var name = "result"
            while (msg.fields.any { it.fieldName == name }) {
                name += "_"
            }
            name
        }

    private fun propertyInfoForField(f: StandardField): PropertyInfo? =
        propertyInfoList.firstOrNull { it.name == f.fieldName }

    fun generate(): PropertySpec {
        val fieldSizes =
            msg.mapFields(
                ctx,
                properties,
                false,
                { std, p ->
                    val propInfo = propertyInfoForField(std)
                    when {
                        propInfo?.repeatedCachingInfo != null ->
                            CodeBlock.of(
                                "$resultVarName·+=·%L",
                                sizeOfRepeatedCaching(std, p, propInfo.repeatedCachingInfo)
                            )

                        propInfo?.mapCachingInfo != null ->
                            CodeBlock.of(
                                "$resultVarName·+=·%L",
                                sizeOfMapCaching(std, p, propInfo.mapCachingInfo)
                            )

                        else ->
                            CodeBlock.of("$resultVarName·+=·%L", sizeOf(std, ctx, property = p))
                    }
                },
                { oneof, std, _ -> sizeofOneof(oneof, std) }
            )

        return PropertySpec.builder(SERIALIZED_SIZE, Int::class)
            .addModifiers(KModifier.PRIVATE)
            .delegate(
                buildCodeBlock {
                    beginControlFlow("lazy")
                    add(
                        if (fieldSizes.isEmpty()) {
                            CodeBlock.of("unknownFields.size()")
                        } else {
                            buildCodeBlock {
                                addStatement("var·$resultVarName·=·0")
                                fieldSizes.forEach { fs -> add(fs) }
                                addStatement("$resultVarName·+=·unknownFields.size()")
                                addStatement(resultVarName)
                            }
                        }
                    )
                    endControlFlow()
                }
            )
            .build()
    }

    private fun sizeofOneof(o: Oneof, f: StandardField) =
        if (f.type == FieldType.String && !f.wrapped) {
            buildCodeBlock {
                add(
                    "$resultVarName·+=·%M(${f.tag}u) + %L",
                    sizeOf,
                    f.sizeOf(CodeBlock.of("%N.%N.wireValue()", o.fieldName, "_${f.fieldName}"))
                )
            }
        } else {
            CodeBlock.of(
                "$resultVarName·+=·%L",
                sizeOf(
                    f,
                    ctx,
                    interceptSizeof(
                        f,
                        CodeBlock.of("%N.%N", o.fieldName, f.fieldName),
                        ctx
                    )
                )
            )
        }

    private fun repeatedWireTypeName(info: RepeatedCachingInfo) =
        when (info) {
            is RepeatedCachingInfo.PlainString -> Bytes::class.asTypeName()
            is RepeatedCachingInfo.Converted -> info.wireTypeName
        }

    private fun mapWireKeyType(f: StandardField, info: MapCachingInfo) =
        info.keyWireTypeName ?: f.mapKey.interceptTypeName(ctx)

    private fun mapWireValueType(f: StandardField, info: MapCachingInfo) =
        info.valueWireTypeName ?: f.mapValue.interceptTypeName(ctx)

    private fun sizeOfRepeatedCaching(
        f: StandardField,
        p: PropertySpec,
        info: RepeatedCachingInfo
    ): CodeBlock {
        val wireType = repeatedWireTypeName(info)
        return buildCodeBlock {
            add("@%T(\"UNCHECKED_CAST\")\n", Suppress::class)
            add("(%N as %T<%T, %T>).let·{·list·->\n", p, LazyConvertingList::class, wireType, com.squareup.kotlinpoet.ANY)
            indent()
            add("(%M(${f.tag}u) * list.size) + ", sizeOf)
            when (val sizeFn = f.type.sizeFn) {
                is SizeFn.Const ->
                    add("(list.size * %L)", sizeFn.size)

                is SizeFn.Method -> {
                    add("run·{·var·sum·=·0;·for·(i·in·list.indices)·sum·+=·%M(list.wireGet(i));·sum·}", sizeFn.method)
                }
            }
            add("\n")
            endControlFlowWithoutNewline()
        }
    }

    private fun sizeOfMapCaching(
        f: StandardField,
        p: PropertySpec,
        info: MapCachingInfo
    ): CodeBlock {
        val wireKeyType = mapWireKeyType(f, info)
        val wireValueType = mapWireValueType(f, info)
        return buildCodeBlock {
            add("@%T(\"UNCHECKED_CAST\")\n", Suppress::class)
            add("(%N as %T<%T, %T>).let·{·map·->\n", p, LazyConvertingMap::class, com.squareup.kotlinpoet.ANY, com.squareup.kotlinpoet.ANY)
            indent()
            add("var·sum·=·0\n")
            val sizeOfCall = sizeOfCall(f.mapKey, f.mapValue, CodeBlock.of("wireK"), CodeBlock.of("wireV"))
            add("map.wireEntryForEach<%T, %T>·{·wireK,·wireV·->\n", wireKeyType, wireValueType)
            indent()
            add("sum·+=·%M(${f.tag}u)·+·%T.%L.let·{·it·+·%M(it.toUInt())·}\n", sizeOf, f.className, sizeOfCall, sizeOf)
            unindent()
            add("}\n")
            add("sum\n")
            unindent()
            add("}")
        }
    }
}

internal fun sizeOf(
    f: StandardField,
    ctx: Context,
    oneOfFieldAccess: CodeBlock? = null,
    mapEntry: Boolean = false,
    property: PropertySpec? = null
): CodeBlock {
    val isCaching = property != null && property.name == "_${f.fieldName}"
    val fieldAccess =
        oneOfFieldAccess
            ?: if (f.repeated) {
                CodeBlock.of("%N", f.fieldName)
            } else {
                interceptSizeof(f, CodeBlock.of("%N", f.fieldName), ctx)
            }

    return when {
        f.isMap -> sizeOfMap(f, fieldAccess)

        f.repeated && f.packed -> {
            namedCodeBlock(
                "sizeOf(${f.tag}u) + " +
                    "%elementsSize:L.let·{·it·+·%sizeOf:M(it.toUInt())·}",
                mapOf(
                    "sizeOf" to sizeOf,
                    "elementsSize" to f.elementsSize()
                )
            )
        }

        f.repeated -> {
            namedCodeBlock(
                "(%sizeOf:M(${f.tag}u) * %name:L.size) + %elementsSize:L",
                mapOf(
                    "sizeOf" to sizeOf,
                    "name" to fieldAccess,
                    "elementsSize" to
                        f.elementsSize(
                            interceptValueAccess(f, ctx, CodeBlock.of("it")),
                            parenthesize = false
                        )
                )
            )
        }

        !mapEntry && isCaching && oneOfFieldAccess == null -> buildCodeBlock {
            add(
                "%M(${f.tag}u) + %L",
                sizeOf,
                f.sizeOf(CodeBlock.of("%N.wireValue()", property!!))
            )
        }

        else -> {
            buildCodeBlock {
                add(
                    "%M(${f.tag}u) + %L",
                    sizeOf,
                    interceptFieldSizeof(f, f.sizeOf(fieldAccess), fieldAccess, ctx)
                )
            }
        }
    }
}

private fun sizeOfMap(f: StandardField, name: CodeBlock): CodeBlock {
    val sizeOfCall = sizeOfCall(f.mapKey, f.mapValue, CodeBlock.of("k"), CodeBlock.of("v"))

    return buildCodeBlock {
        add(
            "%M($name, ${f.tag}u)·{·%L,·%L·->\n",
            sizeOf,
            f.mapKey.loopVar("k"),
            f.mapValue.loopVar("v")
        )
        indent()
        add("%T.%L\n", f.className, sizeOfCall)
        endControlFlowWithoutNewline()
    }
}

private fun StandardField.loopVar(name: String) =
    if (type.sizeFn is SizeFn.Method) {
        name
    } else {
        "_"
    }

private fun StandardField.sizeOf(value: CodeBlock): CodeBlock =
    when (val fn = type.sizeFn) {
        is SizeFn.Const -> CodeBlock.of(fn.size.toString())
        is SizeFn.Method -> CodeBlock.of("%M(%L)", fn.method, value)
    }

internal fun StandardField.elementsSize(
    fieldAccess: CodeBlock = CodeBlock.of("it"),
    parenthesize: Boolean = true
) =
    when (val sizeFn = type.sizeFn) {
        is SizeFn.Const ->
            CodeBlock.of("(%N.size * %L)", fieldName, sizeFn.size)
                .let { if (parenthesize) CodeBlock.of("(%L)", it) else it }

        is SizeFn.Method ->
            CodeBlock.of("%N.sumOf·{·%L·}", fieldName, sizeOf(fieldAccess))
    }
