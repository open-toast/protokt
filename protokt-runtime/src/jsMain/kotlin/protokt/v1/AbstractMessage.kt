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

package protokt.v1

import org.khronos.webgl.Int8Array

@OptIn(OnlyForUseByGeneratedProtoCode::class)
actual abstract class AbstractMessage actual constructor() : Message {
    actual final override fun serialize(): ByteArray {
        val writer = ProtobufJsWriter.create()
        serialize(writer(writer))
        val buf = writer.finish()
        val res = Int8Array(buf.buffer, buf.byteOffset, buf.length).unsafeCast<ByteArray>()
        check(res.size == messageSize()) { "Expected ${messageSize()}, got ${res.size}" }
        return res
    }
}
