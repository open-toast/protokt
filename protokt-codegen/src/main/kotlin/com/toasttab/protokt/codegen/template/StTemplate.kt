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
import kotlin.reflect.full.declaredMemberProperties
import org.stringtemplate.v4.STGroupFile

val stGroupFiles =
    StGroup::class.sealedSubclasses
        .map { requireNotNull(it.objectInstance) { it } }
        .associateWith { STGroupFile(it.fileName) }

abstract class StTemplate<T : Any>(
    val stGroup: StGroup,
    val name: String
)

fun StTemplate<Unit>.render() =
    stGroupFiles.getValue(stGroup).getInstanceOf(name).render()

abstract class Prepare<T : Any>(val parent: StTemplate<T>)

inline fun <reified T : Any> Prepare<T>.prepare(): StRenderable {
    val st = stGroupFiles.getValue(parent.stGroup).getInstanceOf(parent.name)

    T::class.declaredMemberProperties.forEach {
        st.add(it.name, it.get(this as T))
    }

    return StRenderable(st)
}

inline fun <reified T : Any> Prepare<T>.render() =
    prepare().render()
