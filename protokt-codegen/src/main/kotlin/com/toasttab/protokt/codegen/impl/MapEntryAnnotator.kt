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

import com.toasttab.protokt.codegen.impl.STAnnotator.Context
import com.toasttab.protokt.codegen.protoc.Message
import com.toasttab.protokt.codegen.template.Message.Message.MapEntryInfo

internal object MapEntryAnnotator {
    fun annotateMapEntry(m: Message, ctx: Context) =
        if (m.mapEntry) {
            resolveMapEntry(m, ctx).let { (k, v) ->
                MapEntryInfo(true, k, v)
            }
        } else {
            MapEntryInfo(false, "", "")
        }
}
