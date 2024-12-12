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
import protokt.v1.reflect.DOT_GOOGLE_PROTOBUF
import protokt.v1.reflect.PROTOKT_V1
import protokt.v1.reflect.resolvePackage

val PROTOKT_V1_GOOGLE_PROTO = PROTOKT_V1 + DOT_GOOGLE_PROTOBUF

fun packagesByFileName(protoFileList: List<FileDescriptorProto>) =
    protoFileList.associate { it.name to resolvePackage(it.`package`) }
