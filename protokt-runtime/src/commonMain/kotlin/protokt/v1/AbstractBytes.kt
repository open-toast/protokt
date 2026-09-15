/*
 * Copyright (c) 2023 Toast, Inc.
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

@OnlyForUseByGeneratedProtoCode
abstract class AbstractBytes internal constructor(
    internal val value: ByteArray
) {
    val bytes
        get() = clone(value)

    fun isNotEmpty() =
        value.isNotEmpty()

    fun isEmpty() =
        value.isEmpty()

    fun toBytesSlice() =
        BytesSlice(value)

    final override fun equals(other: Any?) =
        other is AbstractBytes && value.contentEquals(other.value)

    final override fun hashCode() =
        value.contentHashCode()

    final override fun toString() =
        renderBytes(value, 0, value.size)

    internal companion object {
        private val EMPTY = Bytes(ByteArray(0))

        fun empty() =
            EMPTY

        fun from(bytes: ByteArray) =
            Bytes(clone(bytes))

        fun from(message: Message) =
            Bytes(message.serialize())
    }
}

private const val BYTE_PREVIEW_LIMIT = 32
private const val HEX_DIGITS = "0123456789abcdef"

internal fun renderBytes(
    bytes: ByteArray,
    offset: Int,
    length: Int,
) =
    buildString {
        append("Bytes(size=")
        append(length)
        append(", hex=\"")
        repeat(minOf(length, BYTE_PREVIEW_LIMIT)) {
            val value = bytes[offset + it].toInt() and 0xff
            append(HEX_DIGITS[value ushr 4])
            append(HEX_DIGITS[value and 0x0f])
        }
        if (length > BYTE_PREVIEW_LIMIT) {
            append("...")
        }
        append("\")")
    }
