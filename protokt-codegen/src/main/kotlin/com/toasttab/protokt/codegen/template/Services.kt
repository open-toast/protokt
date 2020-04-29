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

package com.toasttab.protokt.codegen.template

abstract class ServicesTemplate : StTemplate(StGroup.Services)

object Services {
    object Service : ServicesTemplate() {
        fun render(
            name: String,
            qualifiedName: String,
            descriptor: String,
            methods: List<MethodInfo>
        ) =
            renderArgs(name, qualifiedName, descriptor, methods)

        class MethodInfo(
            val name: String,
            val lowerName: String,
            val body: String,
            val `in`: String,
            val `out`: String
        )
    }

    object Descriptor : ServicesTemplate() {
        fun render(methods: List<String>) =
            renderArgs(methods)
    }

    object Method : ServicesTemplate() {
        fun render(
            name: String,
            type: String,
            `in`: String,
            out: String,
            options: MethodOptions
        ) =
            renderArgs(name, type, `in`, out, options)

        class MethodOptions(
            val requestMashaller: String?,
            val responseMarshaller: String?
        )
    }

    object MethodType : ServicesTemplate() {
        fun render(method: com.toasttab.protokt.codegen.Method) =
            renderArgs(method)
    }
}
