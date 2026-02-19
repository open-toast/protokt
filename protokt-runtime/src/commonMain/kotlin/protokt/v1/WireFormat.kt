/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1

internal object WireFormat {
    // same default as protobuf-java. they allow configuration; we do not, but we could.
    const val DEFAULT_RECURSION_LIMIT = 100

    // error messages matching protobuf-java's InvalidProtocolBufferException
    const val NEGATIVE_SIZE =
        "CodedInputStream encountered an embedded string or message which claimed to have negative size."
    const val TRUNCATED_MESSAGE =
        "While parsing a protocol message, the input ended unexpectedly " +
            "in the middle of a field.  This could mean either that the input has been truncated " +
            "or that an embedded message misreported its own length."
    const val TOO_MANY_LEVELS_OF_NESTING =
        "Protocol message had too many levels of nesting"

    const val WIRETYPE_VARINT = 0
    const val WIRETYPE_FIXED64 = 1
    const val WIRETYPE_LENGTH_DELIMITED = 2
    const val WIRETYPE_START_GROUP = 3
    const val WIRETYPE_END_GROUP = 4
    const val WIRETYPE_FIXED32 = 5

    fun getTagWireType(tag: Int): Int =
        tag and 0x7

    fun getTagFieldNumber(tag: Int): Int =
        tag ushr 3

    fun makeTag(fieldNumber: UInt, wireType: Int): UInt =
        (fieldNumber shl 3) or wireType.toUInt()
}
