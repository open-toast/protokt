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

import com.google.protobuf.CodedInputStream
import java.io.InputStream
import java.nio.ByteBuffer

fun <T : KtMessage> KtDeserializer<T>.deserialize(bytes: BytesSlice) =
    deserialize(
        deserializer(
            CodedInputStream.newInstance(
                bytes.array,
                bytes.offset,
                bytes.length
            )
        )
    )

fun <T : KtMessage> KtDeserializer<T>.deserialize(stream: InputStream) =
    deserialize(deserializer(CodedInputStream.newInstance(stream)))

fun <T : KtMessage> KtDeserializer<T>.deserialize(buffer: ByteBuffer) =
    deserialize(deserializer(CodedInputStream.newInstance(buffer)))
