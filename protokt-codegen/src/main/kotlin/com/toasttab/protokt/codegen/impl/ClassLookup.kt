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

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.memoize
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.protoc.ProtocolContext
import com.toasttab.protokt.ext.Converter
import java.io.File
import java.net.URLClassLoader

internal object ClassLookup {
    val getClass =
        { pClass: PClass, ctx: ProtocolContext ->
            fun loadClass(pClass: PClass) =
                Either.catchingAll {
                    getClassLoader(ctx.classpath)
                        .loadClass(pClass.qualifiedName)
                        .kotlin
                }

            loadClass(pClass).getOrHandle {
                throw Exception("Class not found: ${pClass.qualifiedName}")
            }
        }.memoize()

    // Either.catch does not catch LinkageError, of which NoClassDefFoundError
    // is a subtype. We want to catch those here.
    private fun <R> Either.Companion.catchingAll(
        f: () -> R
    ): Either<Throwable, R> =
        try {
            Either.Right(f())
        } catch (t: Throwable) {
            Either.Left(t)
        }

    val getClassLoader = { classpath: List<String> ->
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
    }.memoize()

    val converters = { classpath: List<String> ->
        val loader = getClassLoader(classpath)
        loader.getResources("META-INF/services/${Converter::class.qualifiedName}")
            .asSequence()
            .flatMap { url ->
                url.openStream()
                    .bufferedReader()
                    .useLines { lines ->
                        lines.map { it.substringBefore("#").trim() }
                            .filter { it.isNotEmpty() }
                            .map { loader.loadClass(it).kotlin.objectInstance as Converter<*, *> }
                            .toList()
                    }
            }
            .toList()
    }.memoize()
}
