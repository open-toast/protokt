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

import arrow.core.None
import arrow.core.orElse
import com.toasttab.protokt.codegen.impl.Annotator.googleProto
import com.toasttab.protokt.codegen.protoc.StandardField
import com.toasttab.protokt.codegen.template.Options.JavaClassNameForWellKnownType

object WellKnownTypes {
    val StandardField.wrapWithWellKnownInterception
        get() =
            options.protokt.wrap.emptyToNone()
                .orElse {
                    if (protoTypeName.startsWith("$googleProto.")) {
                        JavaClassNameForWellKnownType.render(
                            type = protoTypeName.removePrefix("$googleProto.")
                        ).emptyToNone()
                    } else {
                        None
                    }
                }
}
