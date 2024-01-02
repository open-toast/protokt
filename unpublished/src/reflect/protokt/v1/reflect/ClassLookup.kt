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

package protokt.v1.reflect

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table
import protokt.v1.Bytes
import protokt.v1.Converter
import protokt.v1.OptimizedSizeOfConverter
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

internal class ClassLookup(classpath: List<String>) {
    private val classLoader by lazy {
        val current = Thread.currentThread().contextClassLoader

        when {
            classpath.isEmpty() -> current
            else ->
                URLClassLoader(
                    classpath
                        .map { File(it).toURI().toURL() }
                        .toTypedArray(),
                    current
                )
        }
    }

    private val convertersByProtoClassNameAndKotlinClassName by lazy {
        classLoader.getResources("META-INF/services/${Converter::class.qualifiedName}")
            .asSequence()
            .flatMap { url ->
                url.openStream()
                    .bufferedReader()
                    .useLines { lines ->
                        lines.map { it.substringBefore("#").trim() }
                            .filter { it.isNotEmpty() }
                            .map { classLoader.loadClass(it).kotlin.objectInstance as Converter<*, *> }
                            .toList()
                    }
            }.run {
                val table = HashBasedTable.create<String, String, MutableList<Converter<*, *>>>()
                forEach { table.getOrPut(it.wrapped.qualifiedName!!, it.wrapper.qualifiedName!!) { mutableListOf() }.add(it) }
                ImmutableTable.builder<String, String, List<Converter<*, *>>>().putAll(table).build()
            }
    }

    private val classLookup = mutableMapOf<String, KClass<*>>()

    fun properties(canonicalClassName: String): Collection<String> =
        try {
            classLookup.getOrPut(canonicalClassName) {
                classLoader.loadClass(canonicalClassName).kotlin
            }.memberProperties.map { it.name }
        } catch (t: Throwable) {
            throw Exception("Class not found: $canonicalClassName")
        }

    fun converter(protoClassCanonicalName: String, kotlinClassCanonicalName: String): ConverterDetails {
        val converters =
            convertersByProtoClassNameAndKotlinClassName.get(
                protoClassCanonicalName,
                kotlinClassCanonicalName
            ) ?: emptyList()

        require(converters.isNotEmpty()) {
            "No converter found for wrapper type $kotlinClassCanonicalName from type $protoClassCanonicalName"
        }

        val converter =
            converters
                .filterNot { it::class.hasAnnotation<Deprecated>() }
                .firstOrNull()
                ?: converters.first()

        val defaultValueFailure = tryDeserializeDefaultValue(converter)

        if (converter.acceptsDefaultValue) {
            require(defaultValueFailure == null) {
                "Converter $converter claims to work on protobuf default value but it does not; " +
                    "it fails with ${defaultValueFailure!!.stackTraceToString()}"
            }
        } else {
            require(defaultValueFailure != null) {
                "Converter $converter claims not to work on protobuf default value but it does"
            }
        }

        return ConverterDetails(
            converter,
            kotlinClassCanonicalName,
            converter is OptimizedSizeOfConverter<*, *>,
            !converter.acceptsDefaultValue
        )
    }

    companion object {
        fun evaluateProtobufTypeCanonicalName(
            fieldDescriptorTypeName: String,
            canonicalClassName: String,
            type: FieldType,
            fieldName: String
        ): String =
            fieldDescriptorTypeName.takeIf { it.isNotEmpty() }
                ?.let { canonicalClassName }
                // Protobuf primitives have no typeName
                ?: requireNotNull(type.kotlinRepresentation) {
                    "no kotlin representation for type of $fieldName: $type"
                }.qualifiedName!!
    }
}

private fun <T : Any> tryDeserializeDefaultValue(converter: Converter<T, *>): Throwable? {
    fun tryWrap(unwrapped: T) =
        try {
            converter.wrap(unwrapped)
            null
        } catch (t: Throwable) {
            t
        }

    val protoDefault: Any? =
        when (converter.wrapped) {
            Int::class -> 0
            Long::class -> 0L
            UInt::class -> 0u
            ULong::class -> 0uL
            Float::class -> 0.0F
            Double::class -> 0.0
            String::class -> ""
            Bytes::class -> Bytes.empty()
            else -> null
        }

    @Suppress("UNCHECKED_CAST")
    return if (protoDefault == null) null else tryWrap(protoDefault as T)
}

internal class ConverterDetails(
    val converter: Converter<*, *>,
    val kotlinCanonicalClassName: String,
    val optimizedSizeof: Boolean,
    val cannotDeserializeDefaultValue: Boolean
)

private fun <R, C, V> Table<R, C, V>.getOrPut(r: R, c: C, v: () -> V): V =
    get(r, c) ?: v().also { put(r, c, it) }
