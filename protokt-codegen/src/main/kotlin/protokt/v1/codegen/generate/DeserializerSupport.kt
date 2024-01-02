/*
 * Copyright (c) 2021 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import protokt.v1.reflect.FieldType

internal fun deserializeVarInitialState(p: PropertyInfo) =
    if (
        (p.repeated || p.wrapped || p.nullable || p.fieldType == FieldType.Message) &&
        !p.mapEntry
    ) {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun wrapDeserializedValueForConstructor(p: PropertyInfo) =
    if (p.nonNullOption) {
        buildCodeBlock {
            beginControlFlow("requireNotNull(%N)", p.name)
            add("StringBuilder(\"%N\")\n", p.name)
            withIndent {
                add(
                    (
                        ".append(\" specified nonnull with (protokt." +
                            "${if (p.oneof) "oneof" else "property"}).non_null " +
                            "but was null\")"
                        ).bindSpaces()
                )
                add("\n")
            }
            endControlFlowWithoutNewline()
        }
    } else {
        if (p.map) {
            CodeBlock.of("%M(%N)", unmodifiableMap, p.name)
        } else if (p.repeated) {
            CodeBlock.of("%M(%N)", unmodifiableList, p.name)
        } else {
            buildCodeBlock {
                add("%N", p.name)
                if (p.wrapped && !p.nullable) {
                    add(" ?: %L", p.defaultValue)
                }
            }
        }
    }
