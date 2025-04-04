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

package com.toasttab.protokt.rt

import com.google.protobuf.CodedOutputStream
import java.io.OutputStream

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
interface KtMessage {
    val messageSize: Int

    fun serialize(serializer: KtMessageSerializer)

    fun serialize(outputStream: OutputStream) =
        CodedOutputStream.newInstance(outputStream).run {
            serialize(serializer(this))
            flush()
        }

    fun serialize() =
        ByteArray(messageSize).apply {
            serialize(serializer(CodedOutputStream.newInstance(this)))
        }

    @Deprecated("for ABI backwards compatibility only", level = DeprecationLevel.HIDDEN)
    object DefaultImpls {
        @JvmStatic
        fun serialize(message: KtMessage): ByteArray {
            val buf = ByteArray(message.messageSize)
            message.serialize(serializer(CodedOutputStream.newInstance(buf)))
            return buf
        }

        @JvmStatic
        fun serialize(message: KtMessage, outputStream: OutputStream) {
            CodedOutputStream.newInstance(outputStream).run {
                message.serialize(serializer(this))
                flush()
            }
        }
    }
}
