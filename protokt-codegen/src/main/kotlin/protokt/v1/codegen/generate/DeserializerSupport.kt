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
import protokt.v1.Bytes
import protokt.v1.LazyReference
import protokt.v1.StringConverter
import protokt.v1.reflect.FieldType

internal fun deserializeVarInitialState(p: PropertyInfo) =
    if (p.repeated || p.wrapped || p.nullable || p.cachingString || p.fieldType == FieldType.Message) {
        CodeBlock.of("null")
    } else {
        p.defaultValue
    }

internal fun wrapDeserializedValueForConstructor(p: PropertyInfo, fromBuilder: Boolean = false) =
    if (p.isMap) {
        CodeBlock.of("%M(%N)", unmodifiableMap, p.name)
    } else if (p.repeated) {
        CodeBlock.of("%M(%N)", unmodifiableList, p.name)
    } else if (p.cachingString) {
        if (fromBuilder) {
            CodeBlock.of("%T(%N, %T)", LazyReference::class, p.name, StringConverter::class)
        } else {
            CodeBlock.of("%T(%N ?: %T.empty(), %T)", LazyReference::class, p.name, Bytes::class, StringConverter::class)
        }
    } else {
        buildCodeBlock {
            add("%N", p.name)
            if (p.wrapped && !(p.generateNullableBackingProperty || p.nullable)) {
                add(" ?: %L", p.defaultValue)
            }
        }
    }
