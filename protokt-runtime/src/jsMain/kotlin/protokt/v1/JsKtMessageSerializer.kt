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

internal fun serializer(writer: Writer): KtMessageSerializer {
    return object : KtMessageSerializer {
        override fun writeFixed32(i: UInt) {
            writer.fixed32(i.toInt())
        }

        override fun writeSFixed32(i: Int) {
            writer.sfixed32(i)
        }

        override fun writeUInt32(i: UInt) {
            writer.uint32(i.toInt())
        }

        override fun writeSInt32(i: Int) {
            writer.sint32(i)
        }

        override fun writeFixed64(l: ULong) {
            writer.fixed64(protobufjsLong(l.toLong()))
        }

        override fun writeSFixed64(l: Long) {
            writer.sfixed64(protobufjsLong(l))
        }

        override fun writeUInt64(l: ULong) {
            writer.uint64(protobufjsLong(l.toLong()))
        }

        override fun writeSInt64(l: Long) {
            writer.sint64(protobufjsLong(l))
        }

        override fun write(i: Int) {
            writer.int32(i)
        }

        override fun write(l: Long) {
            writer.int64(protobufjsLong(l))
        }

        override fun write(b: Boolean) {
            writer.bool(b)
        }

        override fun write(s: String) {
            writer.string(s)
        }

        override fun write(f: Float) {
            writer.float(f)
        }

        override fun write(d: Double) {
            writer.double(d)
        }

        override fun write(b: ByteArray) {
            writer.bytes(b.asUint8Array())
        }

        override fun write(b: BytesSlice) {
            writer.bytes(b.asUint8Array())
        }
    }
}
