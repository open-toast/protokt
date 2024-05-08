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

package protokt.v1.reflect

import protokt.v1.Bytes

internal object WellKnownTypes {
    fun wrapWithWellKnownInterception(wrap: String?, typeName: String) =
        wrap
            ?: if (typeName.startsWith("$DOT_GOOGLE_PROTOBUF.")) {
                classNameForWellKnownType(typeName.removePrefix("$DOT_GOOGLE_PROTOBUF."))
            } else {
                null
            }

    private fun classNameForWellKnownType(type: String) =
        when (type) {
            "DoubleValue" -> "kotlin.Double"
            "FloatValue" -> "kotlin.Float"
            "Int64Value" -> "kotlin.Long"
            "UInt64Value" -> "kotlin.ULong"
            "Int32Value" -> "kotlin.Int"
            "UInt32Value" -> "kotlin.UInt"
            "BoolValue" -> "kotlin.Boolean"
            "StringValue" -> "kotlin.String"
            "BytesValue" -> Bytes::class.qualifiedName
            else -> null
        }
}
