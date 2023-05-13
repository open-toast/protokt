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

package com.toasttab.protokt.v1

@Suppress("DEPRECATION")
internal class OldToNewAdapter(
    private val deserializer: com.toasttab.protokt.rt.KtMessageDeserializer
) : KtMessageDeserializer {
    override fun readBytes() =
        Bytes(deserializer.readBytes().value)

    override fun readBytesSlice() =
        deserializer.readBytesSlice().let {
            BytesSlice(it.array, it.offset, it.length)
        }

    override fun readDouble() =
        deserializer.readDouble()

    override fun readFixed32() =
        deserializer.readFixed32().toUInt()

    override fun readFixed64() =
        deserializer.readFixed64().toULong()

    override fun readFloat() =
        deserializer.readFloat()

    override fun readInt64() =
        deserializer.readInt64()

    override fun readSFixed32() =
        deserializer.readSFixed32()

    override fun readSFixed64() =
        deserializer.readSFixed64()

    override fun readSInt32() =
        deserializer.readSInt32()

    override fun readSInt64() =
        deserializer.readSInt64()

    override fun readString() =
        deserializer.readString()

    override fun readUInt64() =
        deserializer.readUInt64().toULong()

    override fun readTag() =
        deserializer.readTag()

    override fun readUnknown() =
        deserializer.readUnknown().let {
            when (it.value) {
                is com.toasttab.protokt.rt.VarintVal -> UnknownField.varint(it.fieldNumber, it.value.value.value)
                is com.toasttab.protokt.rt.Fixed32Val -> UnknownField.fixed32(it.fieldNumber, it.value.value.value.toUInt())
                is com.toasttab.protokt.rt.Fixed64Val -> UnknownField.fixed64(it.fieldNumber, it.value.value.value.toULong())
                is com.toasttab.protokt.rt.LengthDelimitedVal -> UnknownField.lengthDelimited(it.fieldNumber, it.value.value.value)
                else -> error("unsupported unknown field type")
            }
        }

    override fun readRepeated(packed: Boolean, acc: KtMessageDeserializer.() -> Unit) {
        deserializer.readRepeated(packed) { acc(this@OldToNewAdapter) }
    }

    override fun <T : KtMessage> readMessage(m: KtDeserializer<T>) =
        throw UnsupportedOperationException()
}
