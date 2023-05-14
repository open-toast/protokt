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
internal class NewToOldAdapter(
    private val deserializer: KtMessageDeserializer
) : com.toasttab.protokt.rt.KtMessageDeserializer {
    override fun readBytes() =
        com.toasttab.protokt.rt.Bytes(deserializer.readBytes().value)

    override fun readBytesSlice() =
        deserializer.readBytesSlice().let {
            com.toasttab.protokt.rt.BytesSlice(it.array, it.offset, it.length)
        }

    override fun readDouble() =
        deserializer.readDouble()

    override fun readFixed32() =
        deserializer.readFixed32().toInt()

    override fun readFixed64() =
        deserializer.readFixed64().toLong()

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
        deserializer.readUInt64().toLong()

    override fun readTag() =
        deserializer.readTag()

    override fun readUnknown() =
        deserializer.readUnknown().let {
            when (it.value) {
                is VarintVal -> com.toasttab.protokt.rt.UnknownField.varint(it.fieldNumber.toInt(), it.value.value.toLong())
                is Fixed32Val -> com.toasttab.protokt.rt.UnknownField.fixed32(it.fieldNumber.toInt(), it.value.value.toInt())
                is Fixed64Val -> com.toasttab.protokt.rt.UnknownField.fixed64(it.fieldNumber.toInt(), it.value.value.toLong())
                is LengthDelimitedVal -> com.toasttab.protokt.rt.UnknownField.lengthDelimited(it.fieldNumber.toInt(), it.value.value.value)
                else -> error("unsupported unknown field type")
            }
        }

    override fun readRepeated(packed: Boolean, acc: com.toasttab.protokt.rt.KtMessageDeserializer.() -> Unit) {
        deserializer.readRepeated(packed) { acc(this@NewToOldAdapter) }
    }

    override fun <T : com.toasttab.protokt.rt.KtMessage> readMessage(m: com.toasttab.protokt.rt.KtDeserializer<T>) =
        throw UnsupportedOperationException()
}
