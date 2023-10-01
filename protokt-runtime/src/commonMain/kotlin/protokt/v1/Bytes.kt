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

package protokt.v1

import kotlin.jvm.JvmStatic

class Bytes internal constructor(
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

    override fun equals(other: Any?) =
        other is Bytes && value.contentEquals(other.value)

    override fun hashCode() =
        value.contentHashCode()

    override fun toString() =
        value.contentToString()

    companion object {
        private val EMPTY = Bytes(ByteArray(0))

        @JvmStatic
        fun empty() =
            EMPTY

        @JvmStatic
        fun from(bytes: ByteArray) =
            Bytes(clone(bytes))

        @JvmStatic
        fun from(message: KtMessage) =
            Bytes(message.serialize())
    }
}

internal expect fun clone(bytes: ByteArray): ByteArray
