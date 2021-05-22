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
import arrow.core.firstOrNone
import com.google.protobuf.DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location
import com.toasttab.protokt.codegen.impl.Annotator.Context

internal object MessageDocumentationAnnotator {
    fun annotateMessageDocumentation(ctx: Context) =
        baseLocation(ctx).cleanDocumentation()

    fun baseLocation(ctx: Context, extraPath: List<Int> = emptyList()) =
        ctx.desc.sourceCodeInfo.locationList
            .filter { it.pathList == basePath(ctx) + extraPath }
            .firstOrNone()

    private fun basePath(ctx: Context): List<Int> {
        val path = mutableListOf<Int>()

        ctx.enclosing.forEachIndexed { idx, it ->
            if (idx == 0) {
                path.add(MESSAGE_TYPE_FIELD_NUMBER)
                path.add(it.index)
            } else {
                path.add(NESTED_TYPE_FIELD_NUMBER)
                path.add(it.index)
            }
        }

        return path
    }
}

internal fun Option<Location>.cleanDocumentation(): List<String> =
    fold(
        { emptyList() },
        {
            it.leadingComments
                .emptyToNone()
                .fold(
                    { emptyList() },
                    { s ->
                        s.substringBeforeLast("\n")
                            .split("\n")
                            .map { line ->
                                // Escape possibly accidentally nested comments
                                //
                                // Will not render correctly inside backticks:
                                // https://youtrack.jetbrains.com/issue/KT-28979
                                line
                                    .replace("/*", "&#47;*")
                                    .replace("*/", "*&#47;")
                            }
                    }
                )
        }
    )
