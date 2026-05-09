/*
 * Copyright (c) 2021 Toast, Inc.
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
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import protokt.v1.Bytes
import protokt.v1.LazyConvertingList
import protokt.v1.LazyConvertingMap
import protokt.v1.LazyReference
import protokt.v1.StringConverter
import protokt.v1.codegen.util.defaultValue
import protokt.v1.reflect.FieldType

internal fun deserializeVarInitialState(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.cachingInfo != null || p.fieldType == FieldType.Message) {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun wrapDeserializedValueForConstructor(p: PropertyInfo, fromBuilder: Boolean = false) =
    when {
        p.mapCachingInfo != null && fromBuilder ->
            // From builder: already a LazyConvertingMap, pass through
            CodeBlock.of("%N", p.name)

        p.mapCachingInfo != null ->
            CodeBlock.of("%M(%N)", freezeMap, p.name)

        p.repeatedCachingInfo != null && fromBuilder ->
            // From builder: already a LazyConvertingList, pass through
            CodeBlock.of("%N", p.name)

        p.repeatedCachingInfo != null ->
            CodeBlock.of("%M(%N)", freezeList, p.name)

        p.isMap ->
            CodeBlock.of("%M(%N)", freezeMap, p.name)

        p.repeated ->
            CodeBlock.of("%M(%N)", freezeList, p.name)

        p.cachingInfo != null ->
            cachingConstructorArg(p, p.cachingInfo, fromBuilder)

        else ->
            buildCodeBlock {
                add("%N", p.name)
                if (p.wrapped && !(p.generateNullableBackingProperty || p.nullable)) {
                    add(" ?: %L", p.defaultValue)
                }
            }
    }

@Suppress("UNUSED_PARAMETER")
internal fun wrapDeserializedBuilderValueForConstructor(p: PropertyInfo) =
    when {
        p.repeatedCachingInfo != null -> {
            val wireType = when (val info = p.repeatedCachingInfo!!) {
                is RepeatedCachingInfo.PlainString -> Bytes::class.asTypeName()
                is RepeatedCachingInfo.Converted -> info.wireTypeName
            }
            val kotlinElementType = (p.propertyType as ParameterizedTypeName).typeArguments[0]
            val converterRef = when (val info = p.repeatedCachingInfo!!) {
                is RepeatedCachingInfo.PlainString -> CodeBlock.of("%T", StringConverter::class)
                is RepeatedCachingInfo.Converted -> CodeBlock.of("%T", info.converterClassName)
            }
            CodeBlock.of(
                "%N?.build()?.let·{·%T<%T,·%T>(it,·%L)·}·?:·emptyList()",
                p.name,
                LazyConvertingList::class,
                wireType,
                kotlinElementType,
                converterRef
            )
        }

        p.mapCachingInfo != null -> {
            val info = p.mapCachingInfo
            val keyConverterRef = if (info.keyConverterClassName != null) CodeBlock.of("%T", info.keyConverterClassName) else CodeBlock.of("null")
            val valueConverterRef = if (info.valueConverterClassName != null) CodeBlock.of("%T", info.valueConverterClassName) else CodeBlock.of("null")
            CodeBlock.of(
                "%N?.build()?.let·{·%T(it,·%L,·%L,·%L,·%L)·}·?:·emptyMap()",
                p.name,
                LazyConvertingMap::class,
                info.keyWrapped,
                info.valueWrapped,
                keyConverterRef,
                valueConverterRef
            )
        }

        p.isMap ->
            CodeBlock.of("%N?.build() ?: emptyMap()", p.name)

        p.repeated ->
            CodeBlock.of("%N?.build() ?: emptyList()", p.name)

        p.cachingInfo != null ->
            cachingConstructorArg(p, p.cachingInfo, fromBuilder = false)

        else ->
            buildCodeBlock {
                add("%N", p.name)
                if (p.wrapped && !(p.generateNullableBackingProperty || p.nullable)) {
                    add(" ?: %L", p.defaultValue)
                }
            }
    }

private fun cachingConstructorArg(p: PropertyInfo, info: CachingFieldInfo, fromBuilder: Boolean): CodeBlock {
    val converterRef = when (info) {
        is CachingFieldInfo.PlainString -> CodeBlock.of("%T", StringConverter::class)
        is CachingFieldInfo.Converted -> CodeBlock.of("%T", info.converterClassName)
    }

    if (fromBuilder) {
        // Builder uses private _<name>Ref properties; pass LazyReference through unchanged
        val refPropName = "_${p.name}Ref"
        if (info.nullable) {
            return CodeBlock.of("%N", refPropName)
        }
        val wireDefault = wireDefault(info, forBuilder = false)
        return CodeBlock.of("%N ?: %T(%L, %L)", refPropName, LazyReference::class, wireDefault, converterRef)
    }

    if (info.nullable) {
        // Message-typed wrappers: LazyReference is nullable, null means absent
        return CodeBlock.of("%N?.let { %T(it, %L) }", p.name, LazyReference::class, converterRef)
    }

    // Deserializer has WireT?; if null use wire default
    val wireDefault = wireDefault(info, forBuilder = false)
    return CodeBlock.of("%T(%N ?: %L, %L)", LazyReference::class, p.name, wireDefault, converterRef)
}

private fun wireDefault(info: CachingFieldInfo, forBuilder: Boolean): CodeBlock =
    when (info) {
        is CachingFieldInfo.PlainString ->
            if (forBuilder) CodeBlock.of("\"\"") else CodeBlock.of("%T.empty()", Bytes::class)

        is CachingFieldInfo.Converted ->
            info.fieldType.defaultValue
    }
