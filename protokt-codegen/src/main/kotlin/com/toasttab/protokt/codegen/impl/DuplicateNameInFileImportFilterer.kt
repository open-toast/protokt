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

import com.google.common.annotations.VisibleForTesting
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.TypeDesc

fun Sequence<Import>.filterClassesWithSameNameAsMessageIn(
    descs: List<TypeDesc>
) =
    filterClassesWithSameNameAsMessageIn(allMessageNames(descs))

@VisibleForTesting
internal fun Sequence<Import>.filterClassesWithSameNameAsMessageIn(
    names: Sequence<String>
) =
    filterNot { it is Import.Class && names.contains(it.pClass.simpleName) }

private fun allMessageNames(descs: List<TypeDesc>) =
    descs.asSequence().flatMap {
        when (val t = it.type.rawType) {
            is Message -> names(t)
            else -> emptySequence()
        }
    }

private fun names(m: Message): Sequence<String> =
    sequenceOf(m.name) +
        m.nestedTypes
            .asSequence()
            .filterIsInstance<Message>()
            .flatMap { names(it) }
