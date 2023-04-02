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

package com.toasttab.protokt

internal fun serializer(writer: Writer): KtMessageSerializer {
    return object : KtMessageSerializer {
        override fun write(i: Fixed32) {
            writer.fixed32(i.value)
        }

        override fun write(i: SFixed32) {
            writer.sfixed32(i.value)
        }

        override fun write(i: UInt32) {
            writer.uint32(i.value)
        }

        override fun write(i: SInt32) {
            writer.sint32(i.value)
        }

        override fun write(i: Int32) {
            writer.int32(i.value)
        }

        override fun write(l: Fixed64) {
            writer.fixed64(l.value.protobufjsLong)
        }

        override fun write(l: SFixed64) {
            writer.sfixed64(l.value.protobufjsLong)
        }

        override fun write(l: UInt64) {
            writer.uint64(l.value.protobufjsLong)
        }

        override fun write(l: SInt64) {
            writer.sint64(l.value.protobufjsLong)
        }

        override fun write(l: Int64) {
            writer.int64(l.value.protobufjsLong)
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
