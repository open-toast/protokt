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
        other is Bytes && value.contentEquals(other.value)

    final override fun hashCode() =
        value.contentHashCode()

    final override fun toString() =
        value.contentToString()

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
