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

object TypeToNative : StTemplate<TypeToNative.prepare>(
    StGroup.Renderers,
    "typeTonativeF"
) {
    class prepare(
        val type: PType
    ) : Prepare<prepare>(TypeToNative)
}

object ReadFunction : StTemplate<ReadFunction.prepare>(
    StGroup.Renderers,
    "readF"
) {
    class prepare(
        val type: PType,
        val builder: String
    ) : Prepare<prepare>(ReadFunction)
}

object Box : StTemplate<Box.prepare>(
    StGroup.Renderers,
    "boxF"
) {
    class prepare(
        val type: PType,
        val def: String
    ) : Prepare<prepare>(Box)
}

object BoxMap : StTemplate<BoxMap.prepare>(
    StGroup.Renderers,
    "boxMapF"
) {
    class prepare(
        val type: PType,
        val box: String
    ) : Prepare<prepare>(BoxMap)
}

object OneOfDefaultValue : StTemplate<Unit>(
    StGroup.Renderers,
    "oneofDefaultValueF"
)

object ConcatWithScope : StTemplate<ConcatWithScope.prepare>(
    StGroup.Renderers,
    "concatWithScopeF"
) {
    class prepare(
        val scopedValue: Params
    ) : Prepare<prepare>(ConcatWithScope)

    class Params(
        val scope: String,
        val value: String
    )
}

object OneofDeserialize : StTemplate<OneofDeserialize.prepare>(
    StGroup.Renderers,
    "oneOfDeserializeF"
) {
    class prepare(
        val oneof: String,
        val name: String,
        val read: String
    ) : Prepare<prepare>(OneofDeserialize)
}

object DefaultValue : StTemplate<DefaultValue.prepare>(
    StGroup.Renderers,
    "defaultValueF"
) {
    class prepare(
        val field: Field,
        val type: PType,
        val name: String
    ) : Prepare<prepare>(DefaultValue)
}

object NonDefaultValue : StTemplate<NonDefaultValue.prepare>(
    StGroup.Renderers,
    "nonDefaultValueF"
) {
    class prepare(
        val field: StandardField,
        val name: String
    ) : Prepare<prepare>(NonDefaultValue)
}

object IterationVar : StTemplate<Unit>(
    StGroup.Renderers,
    "iterationVar"
)

object Serialize : StTemplate<Serialize.prepare>(
    StGroup.Renderers,
    "serializeF"
) {
    class prepare(
        val field: StandardField,
        val name: String,
        val tag: Int,
        val box: String,
        val options: Options
    ) : Prepare<prepare>(Serialize)

    class Options(
        val fieldAccess: String
    )
}

object Deserialize : StTemplate<Deserialize.prepare>(
    StGroup.Renderers,
    "deserializeF"
) {
    class prepare(
        val field: StandardField,
        val type: String,
        val read: String,
        val lhs: String,
        val options: Options?
    ) : Prepare<prepare>(Deserialize)

    class Options(
        val wrapName: String,
        val type: String,
        val oneof: Boolean
    )
}

object Standard : StTemplate<Standard.prepare>(
    StGroup.Renderers,
    "standardF"
) {
    class prepare(
        val field: StandardField,
        val any: Any,
        val nullable: Boolean
    ) : Prepare<prepare>(Standard)
}

object Type : StTemplate<Type.prepare>(
    StGroup.Renderers,
    "typeF"
) {
    class prepare(
        val field: String? = null,
        val any: String,
        val nullable: Boolean,
        val oneof: Boolean
    ) : Prepare<prepare>(Type)
}

object Sizeof : StTemplate<Sizeof.prepare>(
    StGroup.Renderers,
    "sizeof"
) {
    class prepare(
        val name: String,
        val field: StandardField,
        val type: String,
        val options: Options
    ) : Prepare<prepare>(Sizeof)

    class Options(
        val fieldSizeof: String,
        val fieldAccess: Any
    )
}

object FieldSizeof : StTemplate<FieldSizeof.prepare>(
    StGroup.Renderers,
    "fieldSizeof"
) {
    class prepare(
        val name: String,
        val field: StandardField
    ) : Prepare<prepare>(FieldSizeof)
}
