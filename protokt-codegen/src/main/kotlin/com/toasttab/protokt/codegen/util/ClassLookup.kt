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

package com.toasttab.protokt.codegen.util

import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableTable
import com.google.common.collect.Table
import com.squareup.kotlinpoet.ClassName
import com.toasttab.protokt.ext.Converter
import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class ClassLookup(classpath: List<String>) {
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

    private val convertersByWrapperAndWrapped by lazy {
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
                val table = HashBasedTable.create<KClass<*>, KClass<*>, MutableList<Converter<*, *>>>()
                forEach { table.getOrPut(it.wrapper, it.wrapped) { mutableListOf() }.add(it) }
                ImmutableTable.builder<KClass<*>, KClass<*>, List<Converter<*, *>>>().putAll(table).build()
            }
    }

    private val classLookup = mutableMapOf<ClassName, KClass<*>>()

    fun getClass(className: ClassName): KClass<*> =
        try {
            classLookup.getOrPut(className) {
                classLoader.loadClass(className.canonicalName).kotlin
            }
        } catch (t: Throwable) {
            throw Exception("Class not found: ${className.canonicalName}")
        }

    fun converter(wrapper: KClass<*>, wrapped: KClass<*>): Converter<*, *> {
        val converters = convertersByWrapperAndWrapped.get(wrapper, wrapped) ?: emptyList()

        require(converters.isNotEmpty()) {
            "No converter found for wrapper type " +
                "${wrapper.qualifiedName} from type ${wrapped.qualifiedName}"
        }

        return converters
            .filterNot { it::class.hasAnnotation<Deprecated>() }
            .firstOrNull()
            ?: converters.first()
    }
}

private fun <R, C, V> Table<R, C, V>.getOrPut(r: R, c: C, v: () -> V): V =
    get(r, c) ?: v().also { put(r, c, it) }
