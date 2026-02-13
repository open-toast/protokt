/*
 * Copyright (c) 2026 Toast, Inc.
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

@OptIn(OnlyForUseByGeneratedProtoCode::class)
object StringCachingConverter : CachingConverter<Bytes, String> {
    override val wrapperClass = String::class

    override fun wrap(unwrapped: Bytes): String =
        unwrapped.value.decodeToString()

    override fun unwrap(wrapped: String): Bytes =
        Bytes(wrapped.encodeToByteArray())

    override fun writeTo(writer: Writer, value: Any) {
        if (value is Bytes) writer.write(value) else writer.write(value as String)
    }

    override fun sizeOf(value: Any): Int =
        if (value is Bytes) SizeCodecs.sizeOf(value) else SizeCodecs.sizeOf(value as String)

    override fun isDefault(value: Any): Boolean =
        if (value is Bytes) value.isEmpty() else (value as String).isEmpty()

    fun readValidatedBytes(reader: Reader): Bytes {
        val bytes = reader.readBytes()
        validateUtf8(bytes.value)
        return bytes
    }
}
