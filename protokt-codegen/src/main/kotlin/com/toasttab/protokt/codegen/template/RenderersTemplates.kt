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

import com.toasttab.protokt.codegen.template.RenderVariable.Any
import com.toasttab.protokt.codegen.template.RenderVariable.Box
import com.toasttab.protokt.codegen.template.RenderVariable.Builder
import com.toasttab.protokt.codegen.template.RenderVariable.Def
import com.toasttab.protokt.codegen.template.RenderVariable.Field
import com.toasttab.protokt.codegen.template.RenderVariable.Lhs
import com.toasttab.protokt.codegen.template.RenderVariable.Name
import com.toasttab.protokt.codegen.template.RenderVariable.Nullable
import com.toasttab.protokt.codegen.template.RenderVariable.Oneof
import com.toasttab.protokt.codegen.template.RenderVariable.Options
import com.toasttab.protokt.codegen.template.RenderVariable.Read
import com.toasttab.protokt.codegen.template.RenderVariable.ScopedValue
import com.toasttab.protokt.codegen.template.RenderVariable.Tag
import com.toasttab.protokt.codegen.template.RenderVariable.Type

sealed class RenderVariable(
    override val name: String
) : TemplateVariable {
    object Any : RenderVariable("any")
    object Box : RenderVariable("box")
    object Builder : RenderVariable("builder")
    object Def : RenderVariable("def")
    object Field : RenderVariable("field")
    object Lhs : RenderVariable("lhs")
    object Name : RenderVariable("name")
    object Nullable : RenderVariable("nullable")
    object Oneof : RenderVariable("oneof")
    object Options : RenderVariable("options")
    object Read : RenderVariable("read")
    object ScopedValue : RenderVariable("scopedValue")
    object Tag : RenderVariable("tag")
    object Type : RenderVariable("type")
}

object TypeToNative : StTemplate<RenderVariable>(
    RenderersGroup,
    "typeTonativeF",
    setOf(Type)
)

object ReadFunction : StTemplate<RenderVariable>(
    RenderersGroup,
    "readF",
    setOf(Type, Builder)
)

object Box : StTemplate<RenderVariable>(
    RenderersGroup,
    "boxF",
    setOf(Type, Def)
)

object BoxMap : StTemplate<RenderVariable>(
    RenderersGroup,
    "boxMapF",
    setOf(Type, Box)
)

object OneOfDefaultValue : StTemplate<RenderVariable>(
    RenderersGroup,
    "oneofDefaultValueF",
    emptySet()
)

object ConcatWithScope : StTemplate<RenderVariable>(
    RenderersGroup,
    "concatWithScopeF",
    setOf(ScopedValue)
)

object OneOfDeserialize : StTemplate<RenderVariable>(
    RenderersGroup,
    "oneOfDeserializeF",
    setOf(Oneof, Name, Read)
)

object DefaultValue : StTemplate<RenderVariable>(
    RenderersGroup,
    "defaultValueF",
    setOf(Field, Type, Name)
)

object NonDefaultValue : StTemplate<RenderVariable>(
    RenderersGroup,
    "nonDefaultValueF",
    setOf(Field, Name)
)

object IterationVar : StTemplate<RenderVariable>(
    RenderersGroup,
    "iterationVar",
    emptySet()
)

object Serialize : StTemplate<RenderVariable>(
    RenderersGroup,
    "serializeF",
    setOf(Field, Name, Tag, Box, Options)
)

object Deserialize : StTemplate<RenderVariable>(
    RenderersGroup,
    "deserializeF",
    setOf(Field, Type, Read, Lhs, Options)
)

object Standard : StTemplate<RenderVariable>(
    RenderersGroup,
    "standardF",
    setOf(Field, Any, Type, Nullable)
)

object Type : StTemplate<RenderVariable>(
    RenderersGroup,
    "typeF",
    setOf(Field, Any, Nullable, Oneof)
)

object Sizeof : StTemplate<RenderVariable>(
    RenderersGroup,
    "sizeof",
    setOf(Name, Field, Type, Options)
)

object FieldSizeof : StTemplate<RenderVariable>(
    RenderersGroup,
    "fieldSizeof",
    setOf(Name, Field)
)
