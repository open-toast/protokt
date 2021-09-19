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
import com.toasttab.protokt.codegen.protoc.Enum
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.protoc.Oneof
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.protoc.TopLevelType
import com.toasttab.protokt.codegen.protoc.TypeDesc

/**
 * All classes defined in the scope of a message that are used as a field type
 * are added as possible import candidates. When those classes are only used
 * within the message that defines them, there is no need to import them to use
 * them without qualification by their enclosing message type. For example:
 *
 *    class Message(
 *        val enum: SomeEnum // does not need to be `Message.SomeEnum`
 *    ) : KtMessage {
 *        sealed class SomeEnum(...) : KtEnum() {
 *            object OPTION_ONE : SomeEnum(...)
 *        }
 *    }
 *
 * These imports _are_ necessary in other files or equivalently when used in
 * messages other than that in which they are defined.
 */
fun Sequence<Import>.filterNestedClassesDefinedLocally(descs: List<TypeDesc>) =
    filterNot {
        it is Import.Class && it.nested &&
            descsUsing(it, descs).containsOnly(descsDefining(it, descs))
    }

private fun descsUsing(import: Import.Class, descs: List<TypeDesc>) =
    descs.filter { searchTypes(it.type.rawType, import) }.toSet()

private fun searchTypes(t: TopLevelType, import: Import.Class): Boolean =
    t is Message &&
        (searchFields(t, import) || t.nestedTypes.any { searchTypes(it, import) })

private fun searchFields(msg: Message, import: Import.Class) =
    msg.fields.any {
        when (it) {
            is StandardField -> it.typePClass == import.pClass
            is Oneof -> it.fields.any { f -> f.typePClass == import.pClass }
        }
    }

private fun descsDefining(import: Import.Class, descs: List<TypeDesc>) =
    descs.filter {
        it.type.rawType.let { t ->
            import.pkg == kotlinPackage(it) &&
                t is Message && searchMessage(t, t.name, import)
        }
    }.let {
        if (it.isNotEmpty()) {
            // singleOrNull returns null if more than one match rather than throwing
            it.single()
        } else {
            null
        }
    }

private fun searchMessage(msg: Message, name: String, import: Import.Class): Boolean =
    name == import.pClass.nestedName ||
        msg.nestedTypes.any {
            val nestedName = "$name.${it.name}"
            when (it) {
                is Message -> searchMessage(it, nestedName, import)
                is Enum -> nestedName == import.pClass.nestedName
                else -> false
            }
        }

private fun <T> Set<T>.containsOnly(t: T) =
    size == 1 && contains(t)
