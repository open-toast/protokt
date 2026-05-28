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
        _lastTag =
            if (reader.pos == endPosition) {
                0u
            } else {
                val tag = readInt32()
                check(tag ushr 3 != 0) { "Invalid tag" }
                tag.toUInt()
            }
        return _lastTag
    }

    override fun readBytes() =
        Bytes(reader.bytes().asByteArray())

    // Does protobufjs support reading a slice?
    override fun readBytesSlice() =
        readBytes().toBytesSlice()

    override fun readRepeated(packed: Boolean, acc: Reader.() -> Unit) {
        if (!packed || WireFormat.getTagWireType(_lastTag.toInt()) != WireFormat.WIRETYPE_LENGTH_DELIMITED) {
            acc(this)
        } else {
            val length = readInt32()
            val endPosition = reader.pos + length
            while (reader.pos < endPosition) {
                acc(this)
            }
        }
    }

    override fun <T : Message> readMessage(m: Deserializer<T>): T {
        check(++messageDepth <= WireFormat.DEFAULT_RECURSION_LIMIT) { WireFormat.TOO_MANY_LEVELS_OF_NESTING }
        try {
            val oldEndPosition = endPosition
            endPosition = readInt32() + reader.pos
            val ret = m.deserialize(this)
            require(reader.pos == endPosition) {
                "Not at the end of the current message limit as expected"
            }
            endPosition = oldEndPosition
            return ret
        } finally {
            messageDepth--
        }
    }
}
