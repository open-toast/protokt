/*
 * Copyright (c) 2019 Toast, Inc.
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
interface Writer {
    fun writeFixed32(i: UInt)
    fun writeSFixed32(i: Int)
    fun writeUInt32(i: UInt)
    fun writeSInt32(i: Int)
    fun writeFixed64(l: ULong)
    fun writeSFixed64(l: Long)
    fun writeUInt64(l: ULong)
    fun writeSInt64(l: Long)
    fun write(i: Int)
    fun write(l: Long)
    fun write(f: Float)
    fun write(d: Double)
    fun write(s: String)
    fun write(b: Boolean)
    fun write(b: ByteArray)
    fun write(b: BytesSlice)

    fun write(b: Bytes) =
        write(b.value)

    fun writeTag(tag: UInt) =
        also { writeUInt32(tag) }

    fun write(e: Enum) =
        write(e.value)

    fun write(m: Message) {
        write(m.serializedSize())
        m.serialize(this)
    }

    fun writeUnknown(u: UnknownFieldSet) {
        u.unknownFields.forEach { (k, v) -> v.write(k, this) }
    }

    fun toByteArray(): ByteArray
}
