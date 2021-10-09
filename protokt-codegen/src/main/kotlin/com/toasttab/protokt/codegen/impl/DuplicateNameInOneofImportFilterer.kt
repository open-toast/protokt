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

import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.model.PClass
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.TopLevelType

fun Sequence<Import>.filterClassesWithSameNameAsOneofFieldTypeIn(
    types: List<TopLevelType>
) =
    filterNot {
        it is Import.Class &&
            allOneofFieldTypes(types).contains(it.pClass.simpleName)
    }

private fun allOneofFieldTypes(types: List<TopLevelType>) =
    types.asSequence().flatMap {
        when (it) {
            is Message -> typeNames(it)
            else -> emptySequence()
        }
    }

private fun typeNames(m: Message): Sequence<String> =
    m.fields.asSequence().filterIsInstance<Oneof>().flatMap { typeNames(it) } +
        m.nestedTypes
            .asSequence()
            .filterIsInstance<Message>()
            .flatMap { typeNames(it) }

private fun typeNames(o: Oneof) =
    o.fields.asSequence()
        .map { o.fieldTypeNames.getValue(it.name) }
        .map { PClass.fromName(it).simpleName }
