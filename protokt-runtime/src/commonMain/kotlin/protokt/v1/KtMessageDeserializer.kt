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

interface KtMessageDeserializer {
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

    fun readUInt32(): UInt =
        readInt32().toUInt()

    fun <T : KtEnum> readEnum(e: KtEnumDeserializer<T>): T =
        e.from(readInt32())
}
