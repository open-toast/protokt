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

import com.toasttab.protokt.codegen.impl.StRenderable
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import org.stringtemplate.v4.STGroupFile

val stGroupFiles =
    StGroup::class.sealedSubclasses
        .map { requireNotNull(it.objectInstance) { it } }
        .associateWith { STGroupFile(it.fileName) }

abstract class StTemplate(
    val stGroup: StGroup,
    val name: String
)

abstract class PreparableStTemplate<T : Any>(
    stGroup: StGroup,
    name: String
) : StTemplate(stGroup, name)

abstract class ParameterlessStTemplate(
    stGroup: StGroup,
    name: String
) : StTemplate(stGroup, name)

fun ParameterlessStTemplate.render() =
    newSt().render()

abstract class Prepare<T : Any>(val parent: PreparableStTemplate<T>)

inline fun <reified T : Any> Prepare<T>.prepare(): StRenderable {
    val st = parent.newSt()

    T::class.declaredMemberProperties.forEach {
        st.add(it.name, it.get(this as T))
    }

    return StRenderable(st)
}

fun StTemplate.zipRender(vararg params: Any?): String {
    val st = newSt()

    val renderFn = this::class.declaredMemberFunctions
        .single { it.name == (ParameterlessStTemplate::render).name }

    check(renderFn.parameters.size - 1 == params.size) {
        "Wrong number of parameters for ${renderFn.name}: ${params.size}"
    }

    renderFn.parameters.drop(1).zip(params).forEach { (k, v) ->
        st.add(k.name, v)
    }

    return st.render()
}

fun StTemplate.newSt() =
    stGroupFiles.getValue(stGroup).getInstanceOf(name)
