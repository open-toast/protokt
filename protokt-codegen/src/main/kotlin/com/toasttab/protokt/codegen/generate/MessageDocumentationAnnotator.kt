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

package com.toasttab.protokt.codegen.generate

import com.google.protobuf.DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context

fun annotateMessageDocumentation(ctx: Context) =
    baseLocation(ctx)?.cleanDocumentation()

fun baseLocation(ctx: Context, extraPath: List<Int> = emptyList()) =
    ctx.info.sourceCodeInfo.locationList.firstOrNull { it.pathList == basePath(ctx) + extraPath }

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

// todo: see if an empty list passed upwards behaves the same as null, and if so, end this with a call to .orEmpty()
fun Location.cleanDocumentation(): List<String>? =
    leadingComments
        .takeIf { it.isNotEmpty() }
        ?.run {
            substringBeforeLast("\n")
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
