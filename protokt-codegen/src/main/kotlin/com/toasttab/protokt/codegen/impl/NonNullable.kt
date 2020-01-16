/*
 * Copyright (c) 2019 Toast Inc.
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

import com.toasttab.protokt.codegen.Field
import com.toasttab.protokt.codegen.OneOf
import com.toasttab.protokt.codegen.StandardField
import com.toasttab.protokt.rt.PType

internal object NonNullable {
    val Field.nullable
        get() =
            isKotlinRepresentationNullable && !hasNonNullOption

    val Field.hasNonNullOption
        get() =
            when (this) {
                is StandardField -> options.protokt.nonNull
                is OneOf -> options.protokt.nonNull
            }

    private val Field.isKotlinRepresentationNullable
        get() =
            when (this) {
                is StandardField -> type == PType.MESSAGE
                is OneOf -> true
            }
}
