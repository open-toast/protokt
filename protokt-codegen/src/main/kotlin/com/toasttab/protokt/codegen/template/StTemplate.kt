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

import kotlin.reflect.full.declaredMemberFunctions
import org.stringtemplate.v4.STGroupFile

private val stGroupFiles =
    StGroup::class.sealedSubclasses
        .map { requireNotNull(it.objectInstance) { it } }
        .associateWith { STGroupFile(it.fileName) }

abstract class StTemplate(
    private val stGroup: StGroup
) {
    protected fun newSt() =
        this::class.java.simpleName.decapitalize().let {
            requireNotNull(stGroupFiles.getValue(stGroup).getInstanceOf(it)) {
                "template not found: ${stGroup.fileName}/$it"
            }
        }

    protected fun renderArgs(vararg params: Any?): String {
        val st = newSt()

        val renderFn = this::class.declaredMemberFunctions
            .single { it.name == (NoParamStTemplate::render).name }

        val nonReceiverParamCount = renderFn.parameters.size - 1

        check(nonReceiverParamCount == params.size) {
            "Wrong number of parameters for ${renderFn.name}: expected " +
                "${params.size}, got $nonReceiverParamCount " +
                "(${renderFn.parameters.map { it.name }})"
        }

        renderFn.parameters.drop(1).zip(params).forEach { (k, v) ->
            st.add(k.name, v)
        }

        return st.render()
    }
}

abstract class NoParamStTemplate(stGroup: StGroup) : StTemplate(stGroup) {
    fun render() =
        newSt().render()
}
