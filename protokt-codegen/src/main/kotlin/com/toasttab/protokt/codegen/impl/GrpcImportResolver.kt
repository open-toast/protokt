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

import com.toasttab.protokt.codegen.ServiceType
import com.toasttab.protokt.codegen.TypeDesc
import com.toasttab.protokt.codegen.algebra.AST
import java.util.TreeSet

object GrpcImportResolver {
    fun grpcImports(astList: List<AST<TypeDesc>>): Pair<HeaderVar, Any> {
        val imports = TreeSet<String>()

        astList.forEach {
            when (val type = it.data.type.rawType) {
                is ServiceType -> {
                    imports.add("io.grpc.ServiceDescriptor")

                    if (type.methods.isNotEmpty()) {
                        imports.add("io.grpc.MethodDescriptor")
                        imports.add("io.grpc.MethodDescriptor.MethodType")
                        imports.add("io.grpc.MethodDescriptor.generateFullMethodName")
                        imports.add("com.toasttab.protokt.grpc.KtMarshaller")
                    }
                }
            }
        }

        return OthersHeaderVar to imports
    }
}
