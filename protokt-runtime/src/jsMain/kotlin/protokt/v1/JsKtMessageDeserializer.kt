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

internal fun deserializer(reader: Reader): KtMessageDeserializer {
    return object : KtMessageDeserializer {
        var lastTag = 0u
        var endPosition = reader.len

        override fun readDouble() =
            reader.double()

        override fun readFixed32() =
            reader.fixed32().toUInt()

        override fun readFixed64() =
            Long.fromProtobufJsLong(reader.fixed64()).toULong()

        override fun readFloat() =
            reader.float()

        override fun readInt64() =
            Long.fromProtobufJsLong(reader.int64())

        override fun readSFixed32() =
            reader.sfixed32()

        override fun readSFixed64() =
            Long.fromProtobufJsLong(reader.sfixed64())

        override fun readSInt32() =
            reader.sint32()

        override fun readSInt64() =
            Long.fromProtobufJsLong(reader.sint64())

        override fun readString() =
            reader.string()

        override fun readUInt64() =
            Long.fromProtobufJsLong(reader.uint64()).toULong()

        override fun readTag(): UInt {
            lastTag =
                if (reader.pos == endPosition) {
                    0u
                } else {
                    val tag = readInt32()
                    check(tag ushr 3 != 0) { "Invalid tag" }
                    tag.toUInt()
                }
            return lastTag
        }

        override fun readBytes() =
            Bytes(reader.bytes().asByteArray())

        // Does protobufjs support reading a slice?
        override fun readBytesSlice() =
            readBytes().toBytesSlice()

        override fun readUnknown(): UnknownField {
            val fieldNumber = (lastTag.toInt() ushr 3).toUInt()

            return when (tagWireType(lastTag)) {
                0 -> UnknownField.varint(fieldNumber, readInt64())
                1 -> UnknownField.fixed64(fieldNumber, readFixed64())
                2 -> UnknownField.lengthDelimited(fieldNumber, reader.bytes().asByteArray())
                3 -> throw UnsupportedOperationException("WIRETYPE_START_GROUP")
                4 -> throw UnsupportedOperationException("WIRETYPE_START_GROUP")
                5 -> UnknownField.fixed32(fieldNumber, readFixed32())
                else -> error("Unrecognized wire type")
            }
        }

        @Suppress("OVERRIDE_BY_INLINE")
        override inline fun readRepeated(
            packed: Boolean,
            acc: KtMessageDeserializer.() -> Unit
        ) {
            if (!packed || tagWireType(lastTag) != 2) {
                acc(this)
            } else {
                val length = readInt32()
                val endPosition = reader.pos + length
                while (reader.pos < endPosition) {
                    acc(this)
                }
            }
        }

        override fun <T : KtMessage> readMessage(m: KtDeserializer<T>): T {
            val oldEndPosition = endPosition
            endPosition = readInt32() + reader.pos
            val ret = m.deserialize(this)
            require(reader.pos == endPosition) {
                "Not at the end of the current message limit as expected"
            }
            endPosition = oldEndPosition
            return ret
        }
    }
}

private fun tagWireType(tag: UInt) =
    tag.toInt() and ((1 shl 3) - 1)
