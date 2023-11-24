/*
 * Copyright (c) 2022 Toast, Inc.
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

import com.google.protobuf.CodedInputStream
import protokt.v1.NewToOldAdapter
import java.io.InputStream
import java.nio.ByteBuffer

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
interface KtDeserializer<T : KtMessage> {
    fun deserialize(bytes: Bytes): T =
        deserialize(bytes.value)

    fun deserialize(bytes: ByteArray): T =
        deserialize(deserializer(CodedInputStream.newInstance(bytes), bytes))

    fun deserialize(bytes: BytesSlice): T =
        deserialize(deserializer(CodedInputStream.newInstance(bytes.array, bytes.offset, bytes.length)))

    fun deserialize(deserializer: KtMessageDeserializer): T

    fun deserialize(stream: InputStream): T =
        deserialize(deserializer(CodedInputStream.newInstance(stream)))

    fun deserialize(stream: CodedInputStream): T =
        deserialize(deserializer(stream))

    fun deserialize(buffer: ByteBuffer): T =
        deserialize(deserializer(CodedInputStream.newInstance(buffer)))

    fun deserialize(bytes: protokt.v1.Bytes): T =
        deserialize(bytes.value)

    fun deserialize(bytes: protokt.v1.BytesSlice): T =
        deserialize(deserializer(CodedInputStream.newInstance(bytes.array, bytes.offset, bytes.length)))

    fun deserialize(deserializer: protokt.v1.KtMessageDeserializer): T =
        deserialize(NewToOldAdapter(deserializer))

    @Deprecated("for ABI backwards compatibility only", level = DeprecationLevel.HIDDEN)
    object DefaultImpls {
        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, bytes: ByteArray): T =
            deserializer.deserialize(deserializer(CodedInputStream.newInstance(bytes), bytes))

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, bytes: Bytes): T =
            deserializer.deserialize(bytes.value)

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, bytes: BytesSlice): T =
            deserializer.deserialize(deserializer(CodedInputStream.newInstance(bytes.array, bytes.offset, bytes.length)))

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, buffer: ByteBuffer): T =
            deserializer.deserialize(deserializer(CodedInputStream.newInstance(buffer)))

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, stream: CodedInputStream): T =
            deserializer.deserialize(deserializer(stream))

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, stream: InputStream): T =
            deserializer.deserialize(deserializer(CodedInputStream.newInstance(stream)))

        @JvmStatic
        fun <T : KtMessage> deserialize(deserializer: KtDeserializer<T>, messageDeserializer: KtMessageDeserializer): T =
            deserializer.deserialize(messageDeserializer)
    }
}
