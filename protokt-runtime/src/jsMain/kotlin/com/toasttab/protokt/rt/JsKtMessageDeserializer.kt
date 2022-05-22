/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.rt

@Suppress("UNUSED_PARAMETER")
internal fun deserializer(
    bytes: ByteArray
): KtMessageDeserializer {
    return object : KtMessageDeserializer {
        override fun readBool() =
            TODO()

        override fun readDouble() =
            TODO()

        override fun readFixed32() =
            TODO()

        override fun readFixed64() =
            TODO()

        override fun readFloat() =
            TODO()

        override fun readInt32() =
            TODO()

        override fun readInt64() =
            TODO()

        override fun readSFixed32() =
            TODO()

        override fun readSFixed64() =
            TODO()

        override fun readSInt32() =
            TODO()

        override fun readSInt64() =
            TODO()

        override fun readString() =
            TODO()

        override fun readUInt32() =
            TODO()

        override fun readUInt64() =
            TODO()

        override fun readTag() =
            TODO()

        override fun readBytes() =
            TODO()

        override fun readBytesSlice() =
            TODO()

        override fun readUnknown(): UnknownField {
            TODO()
        }

        @Suppress("OVERRIDE_BY_INLINE")
        override inline fun readRepeated(
            packed: Boolean,
            acc: KtMessageDeserializer.() -> Unit
        ) {
            TODO()
        }

        override fun <T : KtEnum> readEnum(e: KtEnumDeserializer<T>) =
            TODO()

        override fun <T : KtMessage> readMessage(m: KtDeserializer<T>): T {
            TODO()
        }
    }
}
