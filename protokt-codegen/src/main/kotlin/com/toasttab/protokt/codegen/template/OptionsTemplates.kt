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

import com.toasttab.protokt.codegen.template.OptionsVariable.Arg
import com.toasttab.protokt.codegen.template.OptionsVariable.Oneof
import com.toasttab.protokt.codegen.template.OptionsVariable.Type
import com.toasttab.protokt.codegen.template.OptionsVariable.WrapName

sealed class OptionsVariable(
    override val name: String
) : TemplateVariable {
    object Arg : OptionsVariable("arg")
    object Oneof : OptionsVariable("oneof")
    object Type : OptionsVariable("type")
    object WrapName : OptionsVariable("wrapName")
}

object WrapField : StTemplate<OptionsVariable>(
    OptionsGroup,
    "wrapField",
    setOf(WrapName, Arg, Type, Oneof)
)

object TypeToJavaClassName : StTemplate<OptionsVariable>(
    OptionsGroup,
    "typeToJavaClassName",
    setOf(Type)
)

object AccessField : StTemplate<OptionsVariable>(
    OptionsGroup,
    "accessField",
    setOf(WrapName, Arg)
)

object SizeofOption : StTemplate<OptionsVariable>(
    OptionsGroup,
    "sizeof",
    setOf(Arg)
)

object BytesSlice : StTemplate<OptionsVariable>(
    OptionsGroup,
    "bytesSlice",
    setOf()
)

object ReadBytesSlice : StTemplate<OptionsVariable>(
    OptionsGroup,
    "readBytesSlice",
    setOf()
)

object DefaultBytesSlice : StTemplate<OptionsVariable>(
    OptionsGroup,
    "defaultBytesSlice",
    setOf()
)

object JavaClassNameForWellKnownType : StTemplate<OptionsVariable>(
    OptionsGroup,
    "javaClassNameForWellKnownType",
    setOf(Type)
)
