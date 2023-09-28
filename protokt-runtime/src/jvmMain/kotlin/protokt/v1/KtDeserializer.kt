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

import com.google.protobuf.CodedInputStream
import java.io.InputStream
import java.nio.ByteBuffer

@Suppress("DEPRECATION")
actual interface KtDeserializer<T : com.toasttab.protokt.rt.KtMessage> : com.toasttab.protokt.rt.KtDeserializer<T> {
    actual override fun deserialize(bytes: Bytes): T

    actual override fun deserialize(bytes: ByteArray): T

    actual override fun deserialize(bytes: BytesSlice): T

    actual override fun deserialize(deserializer: KtMessageDeserializer): T

    override fun deserialize(bytes: com.toasttab.protokt.rt.Bytes): T =
        deserialize(bytes.value)

    override fun deserialize(bytes: com.toasttab.protokt.rt.BytesSlice): T =
        deserialize(deserializer(CodedInputStream.newInstance(bytes.array, bytes.offset, bytes.length)))

    override fun deserialize(deserializer: com.toasttab.protokt.rt.KtMessageDeserializer): T =
        deserialize(OldToNewAdapter(deserializer))

    override fun deserialize(stream: InputStream): T =
        deserialize(deserializer(CodedInputStream.newInstance(stream)))

    override fun deserialize(stream: CodedInputStream): T =
        deserialize(deserializer(stream))

    override fun deserialize(buffer: ByteBuffer): T =
        deserialize(deserializer(CodedInputStream.newInstance(buffer)))
}
