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

@OnlyForUseByGeneratedProtoCode
interface Reader {
    val lastTag: UInt

    fun readBytes(): Bytes
    fun readBytesSlice(): BytesSlice
    fun readDouble(): Double
    fun readFixed32(): UInt
    fun readFixed64(): ULong
    fun readFloat(): Float
    fun readInt64(): Long
    fun readSFixed32(): Int
    fun readSFixed64(): Long
    fun readSInt32(): Int
    fun readSInt64(): Long
    fun readString(): String
    fun readUInt64(): ULong
    fun readTag(): UInt
    fun readRepeated(packed: Boolean, acc: Reader.() -> Unit)
    fun <T : Message> readMessage(m: Deserializer<T>): T

    fun readUnknown(): UnknownField {
        val tag = lastTag.toInt()
        val fieldNumber = WireFormat.getTagFieldNumber(tag).toUInt()
        return when (WireFormat.getTagWireType(tag)) {
            WireFormat.WIRETYPE_VARINT ->
                UnknownField.varint(fieldNumber, readInt64())
            WireFormat.WIRETYPE_FIXED64 ->
                UnknownField.fixed64(fieldNumber, readFixed64())
            WireFormat.WIRETYPE_LENGTH_DELIMITED ->
                UnknownField.lengthDelimited(fieldNumber, readBytes().value)
            WireFormat.WIRETYPE_FIXED32 ->
                UnknownField.fixed32(fieldNumber, readFixed32())
            WireFormat.WIRETYPE_START_GROUP ->
                throw UnsupportedOperationException("WIRETYPE_START_GROUP")
            WireFormat.WIRETYPE_END_GROUP ->
                throw UnsupportedOperationException("WIRETYPE_END_GROUP")
            else ->
                error("Unrecognized wire type")
        }
    }

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

    fun readUInt32(): UInt =
        readInt32().toUInt()

    fun <T : Enum> readEnum(e: EnumDeserializer<T>): T =
        e.deserialize(readInt32())
}
