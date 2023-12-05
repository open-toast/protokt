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

package com.toasttab.protokt.rt

import com.google.protobuf.CodedInputStream
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.WireFormat

@Deprecated("for backwards compatibility only")
@Suppress("DEPRECATION")
interface KtMessageDeserializer {
    fun readBytes(): Bytes
    fun readBytesSlice(): BytesSlice
    fun readDouble(): Double
    fun readFixed32(): Int
    fun readFixed64(): Long
    fun readFloat(): Float
    fun readInt64(): Long
    fun readSFixed32(): Int
    fun readSFixed64(): Long
    fun readSInt32(): Int
    fun readSInt64(): Long
    fun readString(): String
    fun readUInt64(): Long
    fun readTag(): Int
    fun readUnknown(): UnknownField
    fun readRepeated(packed: Boolean, acc: KtMessageDeserializer.() -> Unit)
    fun <T : KtMessage> readMessage(m: KtDeserializer<T>): T

    // protobufjs:
    // Protobuf allows int64 values for bool but reader.bool() reads an int32.
    fun readBool(): Boolean =
        readInt64() != 0L

    // protobufjs:
    // Protobuf allows varint64 values where varint32 values are expected. If
    // larger than 32 bits, discard the upper bits. protobufjs has a bug in its
    // implementation: https://github.com/protobufjs/protobuf.js/issues/1067
    //
    // See CodedInputStream#readRawVarint32.
    fun readInt32(): Int =
        readInt64().toInt()

    fun readUInt32(): Int =
        readInt32()

    fun <T : KtEnum> readEnum(e: KtEnumDeserializer<T>): T =
        e.from(readInt32())
}

@Suppress("DEPRECATION")
fun deserializer(bytes: Bytes) =
    deserializer(bytes.value)

fun deserializer(bytes: ByteArray) =
    deserializer(CodedInputStream.newInstance(bytes), bytes)

@Suppress("DEPRECATION")
internal fun deserializer(
    stream: CodedInputStream,
    bytes: ByteArray? = null
): KtMessageDeserializer {
    return object : KtMessageDeserializer {
        override fun readDouble() =
            stream.readDouble()

        override fun readFixed32() =
            stream.readFixed32()

        override fun readFixed64() =
            stream.readFixed64()

        override fun readFloat() =
            stream.readFloat()

        override fun readInt64() =
            stream.readInt64()

        override fun readSFixed32() =
            stream.readSFixed32()

        override fun readSFixed64() =
            stream.readSFixed64()

        override fun readSInt32() =
            stream.readSInt32()

        override fun readSInt64() =
            stream.readSInt64()

        override fun readString() =
            stream.readString()

        override fun readUInt64() =
            stream.readUInt64()

        override fun readTag() =
            stream.readTag()

        override fun readBytes() =
            Bytes(stream.readByteArray())

        override fun readBytesSlice() =
            if (bytes != null) {
                val ln = stream.readRawVarint32()
                val off = stream.totalBytesRead
                stream.skipRawBytes(ln)
                BytesSlice(bytes, off, ln)
            } else {
                BytesSlice(stream.readByteArray())
            }

        override fun readUnknown(): UnknownField {
            val tag = stream.lastTag
            val fieldNumber = WireFormat.getTagFieldNumber(tag)
            return when (WireFormat.getTagWireType(tag)) {
                WireFormat.WIRETYPE_VARINT ->
                    UnknownField.varint(fieldNumber, stream.readInt64())
                WireFormat.WIRETYPE_FIXED64 ->
                    UnknownField.fixed64(fieldNumber, stream.readFixed64())
                WireFormat.WIRETYPE_LENGTH_DELIMITED ->
                    UnknownField.lengthDelimited(fieldNumber, stream.readByteArray())
                WireFormat.WIRETYPE_FIXED32 ->
                    UnknownField.fixed32(fieldNumber, stream.readFixed32())
                WireFormat.WIRETYPE_START_GROUP ->
                    throw UnsupportedOperationException("WIRETYPE_START_GROUP")
                WireFormat.WIRETYPE_END_GROUP ->
                    throw UnsupportedOperationException("WIRETYPE_END_GROUP")
                else ->
                    throw InvalidProtocolBufferException("Unrecognized wire type")
            }
        }

        @Suppress("OVERRIDE_BY_INLINE")
        override inline fun readRepeated(
            packed: Boolean,
            acc: KtMessageDeserializer.() -> Unit
        ) {
            if (!packed ||
                WireFormat.getTagWireType(stream.lastTag) !=
                WireFormat.WIRETYPE_LENGTH_DELIMITED
            ) {
                acc(this)
            } else {
                stream.readRawVarint32().also { sz ->
                    val limit = stream.pushLimit(sz)
                    while (!stream.isAtEnd) {
                        acc(this)
                    }
                    stream.popLimit(limit)
                }
            }
        }

        override fun <T : KtMessage> readMessage(m: KtDeserializer<T>): T {
            val limit = stream.pushLimit(stream.readRawVarint32())
            val res = m.deserialize(this)
            require(stream.isAtEnd)
            stream.popLimit(limit)
            return res
        }
    }
}
