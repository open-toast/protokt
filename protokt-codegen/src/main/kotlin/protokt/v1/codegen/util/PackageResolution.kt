/*
 * Copyright (c) 2020 Toast, Inc.
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

package protokt.v1.codegen.util

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.squareup.kotlinpoet.ClassName
import protokt.v1.Bytes

const val googleProtobuf = "google.protobuf"

val protoktV1 = Bytes::class.java.`package`.name
val protoktV1GoogleProto = Bytes::class.java.`package`.name + "." + googleProtobuf

fun packagesByFileName(protoFileList: List<FileDescriptorProto>) =
    protoFileList.associate { it.name to resolvePackage(it) }

private fun resolvePackage(fdp: FileDescriptorProto) =
    if (fdp.`package`.startsWith("protokt.v1")) {
        fdp.`package`
    } else {
        "protokt.v1." + fdp.`package`
    }

fun requalifyProtoType(typeName: String): ClassName =
    // type name might have a `.` prefix
    ClassName.bestGuess(
        if (typeName.startsWith(".protokt.v1")) {
            typeName.removePrefix(".")
        } else {
            "protokt.v1." + typeName.removePrefix(".")
        }
    )
