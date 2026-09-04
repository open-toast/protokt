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

@OptIn(OnlyForUseByGeneratedProtoCode::class)
internal class ProtobufJsReader(
    private val reader: ProtobufJsReaderAdapter
) : Reader {
    private var _lastTag = 0u
    private var endPosition = reader.len
    private var messageDepth: Int = 0

    override val lastTag: UInt
        get() = _lastTag

    override fun readDouble(): Double {
        checkAvailable(8)
        return decode { reader.double() }
    }

    override fun readFixed32(): UInt {
        checkAvailable(4)
        return decode { reader.fixed32().toUInt() }
    }

    override fun readFixed64(): ULong {
        checkAvailable(8)
        return decode { Long.fromProtobufJsLong(reader.fixed64()).toULong() }
    }

    override fun readFloat(): Float {
        checkAvailable(4)
        return decode { reader.float() }
    }

    override fun readInt64() =
        decode { Long.fromProtobufJsLong(reader.int64()) }

    override fun readSFixed32(): Int {
        checkAvailable(4)
        return decode { reader.sfixed32() }
    }

    override fun readSFixed64(): Long {
        checkAvailable(8)
        return decode { Long.fromProtobufJsLong(reader.sfixed64()) }
    }

    override fun readSInt32() =
        decode { reader.sint32() }

    override fun readSInt64() =
        decode { Long.fromProtobufJsLong(reader.sint64()) }

    override fun readString() =
        decode { reader.string() }

    override fun readUInt64() =
        decode { Long.fromProtobufJsLong(reader.uint64()).toULong() }

    override fun readTag(): UInt {
        _lastTag =
            if (reader.pos == endPosition) {
                0u
            } else {
                protoktCheck(reader.pos <= endPosition) { WireFormat.TRUNCATED_MESSAGE }
                val tag = readInt32()
                protoktCheck(tag ushr 3 != 0) { "Invalid tag: $tag" }
                tag.toUInt()
            }
        return _lastTag
    }

    override fun readBytes() =
        decode { Bytes(reader.bytes().asByteArray()) }

    // Does protobufjs support reading a slice?
    override fun readBytesSlice() =
        readBytes().toBytesSlice()

    override fun readRepeated(packed: Boolean, acc: Reader.() -> Unit) {
        if (!packed || WireFormat.getTagWireType(_lastTag.toInt()) != WireFormat.WIRETYPE_LENGTH_DELIMITED) {
            acc(this)
        } else {
            val length = readInt32()
            val packedEndPosition = checkedEndPosition(length)
            while (reader.pos < packedEndPosition) {
                acc(this)
            }
            protoktCheck(reader.pos == packedEndPosition) { WireFormat.TRUNCATED_MESSAGE }
        }
    }

    override fun <T : Message> readMessage(m: Deserializer<T>): T {
        protoktCheck(messageDepth < WireFormat.DEFAULT_RECURSION_LIMIT) { WireFormat.TOO_MANY_LEVELS_OF_NESTING }
        messageDepth++
        try {
            val oldEndPosition = endPosition
            endPosition = checkedEndPosition(readInt32())
            try {
                val result = m.deserialize(this)
                protoktRequire(reader.pos == endPosition) { WireFormat.MESSAGE_NOT_FULLY_CONSUMED }
                return result
            } finally {
                endPosition = oldEndPosition
            }
        } finally {
            messageDepth--
        }
    }

    private fun checkAvailable(size: Int) {
        protoktCheck(size <= endPosition - reader.pos) { WireFormat.TRUNCATED_MESSAGE }
    }

    private fun checkedEndPosition(length: Int): Int {
        protoktCheck(length >= 0) { WireFormat.NEGATIVE_SIZE }
        protoktCheck(length <= endPosition - reader.pos) { WireFormat.TRUNCATED_MESSAGE }
        return reader.pos + length
    }
}
