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

import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.jvm.JvmStatic

actual class Bytes internal actual constructor(value: ByteArray) : AbstractBytes(value) {
    fun asReadOnlyBuffer(): ByteBuffer =
        ByteBuffer.wrap(value).asReadOnlyBuffer()

    fun inputStream(): InputStream =
        value.inputStream()

    actual companion object {
        @JvmStatic
        actual fun empty() =
            AbstractBytes.empty()

        @JvmStatic
        actual fun from(bytes: ByteArray) =
            AbstractBytes.from(bytes)

        @JvmStatic
        actual fun from(message: KtMessage) =
            AbstractBytes.from(message)

        @JvmStatic
        fun from(stream: InputStream) =
            AbstractBytes.from(stream.readBytes())
    }
}

internal actual fun clone(bytes: ByteArray) =
    bytes.clone()
