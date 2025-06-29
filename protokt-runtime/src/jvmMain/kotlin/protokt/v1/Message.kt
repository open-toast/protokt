/*
 * Copyright (c) 2019 Toast, Inc.
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

import com.google.protobuf.CodedOutputStream
import java.io.OutputStream

@OptIn(OnlyForUseByGeneratedProtoCode::class)
actual interface Message {
    actual fun messageSize(): Int

    actual fun serialize(writer: Writer)

    actual fun serialize(): ByteArray

    fun serialize(outputStream: OutputStream) =
        CodedOutputStream.newInstance(outputStream).run {
            serialize(writer(this))
            flush()
        }
}
