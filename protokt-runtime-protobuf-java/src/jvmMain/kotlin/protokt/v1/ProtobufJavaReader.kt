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

import com.google.protobuf.CodedInputStream

@OptIn(OnlyForUseByGeneratedProtoCode::class)
internal class ProtobufJavaReader(
    private val stream: CodedInputStream,
    private val bytes: ByteArray? = null
) : Reader {
    private var messageDepth: Int = 0

    override val lastTag: UInt
        get() = stream.lastTag.toUInt()

    override fun readDouble() =
        decode { stream.readDouble() }

    override fun readFixed32() =
        decode { stream.readFixed32().toUInt() }

    override fun readFixed64() =
        decode { stream.readFixed64().toULong() }

    override fun readFloat() =
        decode { stream.readFloat() }

    override fun readInt64() =
        decode { stream.readInt64() }

    override fun readSFixed32() =
        decode { stream.readSFixed32() }

    override fun readSFixed64() =
        decode { stream.readSFixed64() }

    override fun readSInt32() =
        decode { stream.readSInt32() }

    override fun readSInt64() =
        decode { stream.readSInt64() }

    override fun readString() =
        decode { stream.readString() }

    override fun readUInt64() =
        decode { stream.readUInt64().toULong() }

    override fun readTag() =
        decode { stream.readTag().toUInt() }

    override fun readBytes() =
        decode { Bytes(stream.readByteArray()) }

    override fun readBytesSlice() =
        if (bytes != null) {
            val ln = decode { stream.readRawVarint32() }
            val off = stream.totalBytesRead
            decode { stream.skipRawBytes(ln) }
            BytesSlice(bytes, off, ln)
        } else {
            decode { BytesSlice(stream.readByteArray()) }
        }

    override fun readRepeated(packed: Boolean, acc: Reader.() -> Unit) {
        if (!packed || WireFormat.getTagWireType(lastTag.toInt()) != WireFormat.WIRETYPE_LENGTH_DELIMITED) {
            acc(this)
        } else {
            decode { stream.readRawVarint32() }.also { size ->
                val limit = decode { stream.pushLimit(size) }
                try {
                    while (stream.bytesUntilLimit > 0) {
                        acc(this)
                    }
                } finally {
                    stream.popLimit(limit)
                }
            }
        }
    }

    override fun <T : Message> readMessage(m: Deserializer<T>): T {
        protoktCheck(messageDepth < WireFormat.DEFAULT_RECURSION_LIMIT) { WireFormat.TOO_MANY_LEVELS_OF_NESTING }
        messageDepth++
        try {
            val length = decode { stream.readRawVarint32() }
            val limit = decode { stream.pushLimit(length) }
            try {
                val result = m.deserialize(this)
                protoktRequire(stream.bytesUntilLimit == 0) { WireFormat.MESSAGE_NOT_FULLY_CONSUMED }
                return result
            } finally {
                stream.popLimit(limit)
            }
        } finally {
            messageDepth--
        }
    }
}
