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

import protokt.v1.Converter
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

internal class ClassLookup private constructor(
    private val classLoader: ClassLoader,
    private val explicitConverters: List<Converter<*, *>>?,
    private val converterSource: String,
) {
    constructor(classpath: List<String>) : this(
        classLoader(classpath),
        null,
        if (classpath.isEmpty()) "thread context class loader" else "configured classpath",
    )

    private val convertersByProtoClassNameAndKotlinClassName: Map<String, Map<String, List<Converter<*, *>>>> by lazy {
        (explicitConverters ?: loadConverters(classLoader))
            .fold(mutableMapOf<String, MutableMap<String, MutableList<Converter<*, *>>>>()) { acc, converter ->
                acc.apply {
                    getOrPut(converter.wrapped.qualifiedName!!, ::mutableMapOf)
                        .getOrPut(converter.wrapper.qualifiedName!!, ::mutableListOf)
                        .add(converter)
                }
            }
    }

    private val classLookup = mutableMapOf<String, KClass<*>>()

    fun properties(canonicalClassName: String): Collection<KProperty<*>> =
        try {
            classLookup.getOrPut(canonicalClassName) {
                classLoader.loadClass(canonicalClassName).kotlin
            }.memberProperties
        } catch (t: Throwable) {
            throw Exception("Class not found: $canonicalClassName")
        }

    fun converter(protoClassCanonicalName: String, kotlinClassCanonicalName: String): ConverterDetails {
        val converters =
            convertersByProtoClassNameAndKotlinClassName[protoClassCanonicalName]
                ?.get(kotlinClassCanonicalName)
                .orEmpty()

        require(converters.isNotEmpty()) {
            "No converter found for wrapper type $kotlinClassCanonicalName from protobuf type " +
                "$protoClassCanonicalName using $converterSource"
        }

        val converter =
            converters
                .filterNot { it::class.hasAnnotation<Deprecated>() }
                .firstOrNull()
                ?: converters.first()

        return ConverterDetails(
            converter,
            kotlinClassCanonicalName
        )
    }

    companion object {
        fun fromClassLoader(classLoader: ClassLoader): ClassLookup =
            ClassLookup(classLoader, null, "class loader ${classLoader.javaClass.name}")

        fun fromConverters(converters: Iterable<Converter<*, *>>): ClassLookup =
            ClassLookup(defaultClassLoader(), converters.toList(), "explicit converter registry")

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

        private fun classLoader(classpath: List<String>): ClassLoader {
            val current = defaultClassLoader()
            return if (classpath.isEmpty()) {
                current
            } else {
                URLClassLoader(
                    classpath
                        .map { File(it).toURI().toURL() }
                        .toTypedArray(),
                    current,
                )
            }
        }

        private fun defaultClassLoader(): ClassLoader =
            Thread.currentThread().contextClassLoader ?: ClassLookup::class.java.classLoader

        private fun loadConverters(classLoader: ClassLoader): List<Converter<*, *>> =
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
                }.toList()
    }
}

internal class ConverterDetails(
    val converter: Converter<*, *>,
    val kotlinCanonicalClassName: String
)
