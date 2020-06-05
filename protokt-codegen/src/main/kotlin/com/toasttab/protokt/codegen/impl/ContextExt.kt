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

import arrow.core.Option
import arrow.core.extensions.list.foldable.firstOption
import arrow.core.toOption
import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.protoc.Message

internal fun Context.stripRootMessageNamePrefix(s: String) =
    stripPrefix(enclosing.firstOption(), s)

internal fun Context.stripEnclosingMessageNamePrefix(s: String) =
    stripPrefix(enclosing.lastOrNull().toOption(), s)

private fun stripPrefix(o: Option<Message>, s: String) =
    o.map { it.name }
        .fold(
            { s },
            {
                if (s.startsWith(it)) {
                    s.substringAfter("$it.")
                } else {
                    s
                }
            }
        )
