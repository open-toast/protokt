/*
 * Copyright (c) 2020 Toast Inc.
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

import com.github.andrewoma.dexx.kollection.ImmutableSet
import com.github.andrewoma.dexx.kollection.immutableSetOf
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.protoc.Method
import com.toasttab.protokt.codegen.protoc.Service
import com.toasttab.protokt.grpc.KtMarshaller
import io.grpc.MethodDescriptor
import io.grpc.ServiceDescriptor

class ServiceImportResolver(
    private val service: Service
) {
    fun imports(): ImmutableSet<Import> =
        immutableSetOf(pclass(ServiceDescriptor::class)) +
            methodImports()

    private fun methodImports() =
        if (service.methods.isNotEmpty()) {
            setOf(
                pclass(MethodDescriptor::class),
                pclass(MethodDescriptor.MethodType::class),
                Import.ClassMethod(
                    PClass.fromClass(MethodDescriptor::class),
                    "generateFullMethodName"
                )
            ) +
                service.methods.flatMap { methodImports(it) } +
                possibleKtMarshaller(service.methods)
        } else {
            emptySet()
        }

    private fun methodImports(method: Method) =
        setOf(
            Import.Class(method.inputType),
            Import.Class(method.outputType)
        )

    private fun possibleKtMarshaller(methods: List<Method>) =
        if (
            methods.any {
                it.options.protokt.requestMarshaller.isEmpty() ||
                    it.options.protokt.responseMarshaller.isEmpty()
            }
        ) {
            setOf(pclass(KtMarshaller::class))
        } else {
            emptySet()
        }
}
