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

import com.toasttab.protokt.codegen.model.FieldType
import com.toasttab.protokt.codegen.protoc.Field
import com.toasttab.protokt.codegen.protoc.StandardField

abstract class RenderersTemplate : StTemplate(StGroup.Renderers)

object Renderers {
    object Read : RenderersTemplate() {
        fun render(type: FieldType, builder: String) =
            renderArgs(type, builder)
    }

    object Box : RenderersTemplate() {
        fun render(type: FieldType, def: String) =
            renderArgs(type, def)
    }

    object BoxMap : RenderersTemplate() {
        fun render(type: FieldType, box: String, options: Options) =
            renderArgs(type, box, options)

        class Options(
            val keyWrap: String?,
            val valueWrap: String?,
            val valueType: FieldType
        )
    }

    object ConcatWithScope : RenderersTemplate() {
        fun render(scope: String, value: String) =
            renderArgs(scope, value)
    }

    object DefaultValue : RenderersTemplate() {
        fun render(field: Field, type: FieldType, name: String) =
            renderArgs(field, type, name)
    }

    object NonDefaultValue : RenderersTemplate() {
        fun render(field: StandardField, name: String) =
            renderArgs(field, name)
    }

    object IterationVar : NoParamStTemplate(StGroup.Renderers)

    object Serialize : RenderersTemplate() {
        fun render(
            field: StandardField,
            name: String,
            tag: Int,
            box: String,
            options: Options
        ) =
            renderArgs(field, name, tag, box, options)

        class Options(
            val fieldAccess: String
        )
    }

    object Deserialize : RenderersTemplate() {
        fun render(
            field: StandardField,
            read: String,
            lhs: String,
            packed: Boolean,
            options: Options?
        ) =
            renderArgs(field, read, lhs, packed, options)

        class Options(
            val wrapName: String,
            val keyWrap: String?,
            val valueWrap: String?,
            val valueType: FieldType?,
            val type: String,
            val oneof: Boolean
        )
    }

    object Standard : RenderersTemplate() {
        fun render(field: StandardField, any: Any) =
            renderArgs(field, any)
    }

    object Sizeof : RenderersTemplate() {
        fun render(
            name: String,
            field: StandardField,
            type: String,
            options: Options
        ) =
            renderArgs(name, field, type, options)

        class Options(
            val fieldSizeof: String,
            val fieldAccess: Any,
            val keyAccess: String?,
            val valueAccess: String?,
            val valueType: FieldType?
        )
    }

    object FieldSizeof : RenderersTemplate() {
        fun render(name: String, field: StandardField) =
            renderArgs(name, field)
    }
}
