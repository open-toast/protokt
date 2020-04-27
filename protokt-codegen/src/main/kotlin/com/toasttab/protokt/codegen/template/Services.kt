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

object Service : PreparableStTemplate<Service.prepare>(
    StGroup.Services,
    "service"
) {
    class prepare(
        val name: String,
        val qualifiedName: String,
        val descriptor: String,
        val methods: List<MethodInfo>
    ) : Prepare<prepare>(Service)

    class MethodInfo(
        val name: String,
        val lowerName: String,
        val body: String,
        val `in`: String,
        val `out`: String
    )
}

object Descriptor : StTemplate(
    StGroup.Services,
    "descriptor"
) {
    fun render(methods: List<String>) =
        zipRender(methods)
}

object Method : StTemplate(
    StGroup.Services,
    "method"
) {
    fun render(name: String, type: String, `in`: String, out: String) =
        zipRender(name, type, `in`, out)
}

object MethodType : StTemplate(
    StGroup.Services,
    "methodType"
) {
    fun render(method: com.toasttab.protokt.codegen.Method) =
        zipRender(method)
}
