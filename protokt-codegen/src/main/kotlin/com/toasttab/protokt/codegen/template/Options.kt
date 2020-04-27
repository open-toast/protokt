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

import com.toasttab.protokt.rt.PType

object WrapField : StTemplate<WrapField.prepare>(
    StGroup.Options,
    "wrapField"
) {
    class prepare(
        val wrapName: String,
        val arg: String,
        val type: PType,
        val oneof: Boolean
    ) : Prepare<prepare>(WrapField)
}

object TypeToJavaClassName : StTemplate<TypeToJavaClassName.prepare>(
    StGroup.Options,
    "typeToJavaClassName"
) {
    class prepare(
        val type: PType
    ) : Prepare<prepare>(TypeToJavaClassName)
}

object AccessField : StTemplate<AccessField.prepare>(
    StGroup.Options,
    "accessField"
) {
    class prepare(
        val wrapName: String,
        val arg: String
    ) : Prepare<prepare>(AccessField)
}

object SizeofOption : StTemplate<SizeofOption.prepare>(
    StGroup.Options,
    "sizeof"
) {
    class prepare(
        val arg: String
    ) : Prepare<prepare>(SizeofOption)
}

object BytesSlice : StTemplate<Unit>(
    StGroup.Options,
    "bytesSlice"
)

object ReadBytesSlice : StTemplate<Unit>(
    StGroup.Options,
    "readBytesSlice"
)

object DefaultBytesSlice : StTemplate<Unit>(
    StGroup.Options,
    "defaultBytesSlice"
)

object JavaClassNameForWellKnownType :
    StTemplate<JavaClassNameForWellKnownType.prepare>(
    StGroup.Options,
    "javaClassNameForWellKnownType"
) {
    class prepare(
        val type: String
    ) : Prepare<prepare>(JavaClassNameForWellKnownType)
}
