/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.reflect

import protokt.v1.Bytes

internal val PROTOKT_V1 = Bytes::class.java.`package`.name
internal const val DOT_GOOGLE_PROTOBUF = ".google.protobuf"

internal fun typeName(protoTypeName: String, fieldType: FieldType): String {
    val fullyProtoQualified = protoTypeName.startsWith(".")

    return if (fullyProtoQualified) {
        requalifyProtoType(protoTypeName)
    } else {
        protoTypeName.let {
            it.ifEmpty {
                fieldType.protoktFieldType.qualifiedName!!
            }
        }
    }
}

internal fun requalifyProtoType(typeName: String) =
    // type name might have a `.` prefix
    if (typeName.startsWith(".$PROTOKT_V1")) {
        typeName.removePrefix(".")
    } else {
        "$PROTOKT_V1." + typeName.removePrefix(".")
    }

internal fun resolvePackage(pkg: String) =
    if (pkg.startsWith(PROTOKT_V1)) {
        pkg
    } else {
        "$PROTOKT_V1.$pkg"
    }
