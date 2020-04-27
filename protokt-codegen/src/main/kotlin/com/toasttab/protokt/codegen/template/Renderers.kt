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

import com.toasttab.protokt.codegen.Field
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.rt.PType

object TypeToNative : StTemplate(
    StGroup.Renderers,
    "typeToNativeF"
) {
    fun render(type: PType) =
        zipRender(type)
}

object ReadFunction : StTemplate(
    StGroup.Renderers,
    "readF"
) {
    fun render(type: PType, builder: String) =
        zipRender(type, builder)
}

object Box : StTemplate(
    StGroup.Renderers,
    "boxF"
) {
    fun render(type: PType, def: String) =
        zipRender(type, def)
}

object BoxMap : StTemplate(
    StGroup.Renderers,
    "boxMapF"
) {
    fun render(type: PType, box: String) =
        zipRender(type, box)
}

object OneOfDefaultValue : ParameterlessStTemplate(
    StGroup.Renderers,
    "oneofDefaultValueF"
)

object ConcatWithScope : StTemplate(
    StGroup.Renderers,
    "concatWithScopeF"
) {
    fun render(scope: String, value: String) =
        zipRender(scope, value)
}

object OneofDeserialize : StTemplate(
    StGroup.Renderers,
    "oneOfDeserializeF"
) {
    fun render(oneof: String, name: String, read: String) =
        zipRender(oneof, name, read)
}

object DefaultValue : StTemplate(
    StGroup.Renderers,
    "defaultValueF"
) {
    fun render(field: Field, type: PType, name: String) =
        zipRender(field, type, name)
}

object NonDefaultValue : StTemplate(
    StGroup.Renderers,
    "nonDefaultValueF"
) {
    fun render(field: StandardField, name: String) =
        zipRender(field, name)
}

object IterationVar : ParameterlessStTemplate(
    StGroup.Renderers,
    "iterationVar"
)

object Serialize : StTemplate(
    StGroup.Renderers,
    "serializeF"
) {
    fun render(
        field: StandardField,
        name: String,
        tag: Int,
        box: String,
        options: Options
    ) =
        zipRender(field, name, tag, box, options)

    class Options(
        val fieldAccess: String
    )
}

object Deserialize : StTemplate(
    StGroup.Renderers,
    "deserializeF"
) {
    fun render(
        field: StandardField,
        type: String,
        read: String,
        lhs: String,
        options: Options?
    ) =
        zipRender(field, type, read, lhs, options)

    class Options(
        val wrapName: String,
        val type: String,
        val oneof: Boolean
    )
}

object Standard : StTemplate(
    StGroup.Renderers,
    "standardF"
) {
    fun render(field: StandardField, any: Any, nullable: Boolean) =
        zipRender(field, any, nullable)
}

object Type : StTemplate(
    StGroup.Renderers,
    "typeF"
) {
    fun render(
        field: String? = null,
        any: String,
        nullable: Boolean,
        oneof: Boolean
    ) =
        zipRender(field, any, nullable, oneof)
}

object Sizeof : StTemplate(
    StGroup.Renderers,
    "sizeof"
) {
    fun render(
        name: String,
        field: StandardField,
        type: String,
        options: Options
    ) =
        zipRender(name, field, type, options)

    class Options(
        val fieldSizeof: String,
        val fieldAccess: Any
    )
}

object FieldSizeof : StTemplate(
    StGroup.Renderers,
    "fieldSizeof"
) {
    fun render(name: String, field: StandardField) =
        zipRender(name, field)
}
