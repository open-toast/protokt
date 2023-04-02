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

import com.google.protobuf.CodedOutputStream

actual abstract class AbstractKtMessage actual constructor() : KtMessage {
    actual final override fun serialize(): ByteArray {
        val buf = ByteArray(messageSize)
        serialize(serializer(CodedOutputStream.newInstance(buf)))
        return buf
    }

    @Suppress("DEPRECATION")
    final override fun serialize(serializer: com.toasttab.protokt.rt.KtMessageSerializer) {
        serialize(
            object : KtMessageSerializer {
                override fun write(i: Fixed32) {
                    serializer.write(com.toasttab.protokt.rt.Fixed32(i.value))
                }

                override fun write(i: SFixed32) {
                    serializer.write(com.toasttab.protokt.rt.SFixed32(i.value))
                }

                override fun write(i: UInt32) {
                    serializer.write(com.toasttab.protokt.rt.UInt32(i.value))
                }

                override fun write(i: SInt32) {
                    serializer.write(com.toasttab.protokt.rt.SInt32(i.value))
                }

                override fun write(i: Int32) {
                    serializer.write(com.toasttab.protokt.rt.Int32(i.value))
                }

                override fun write(l: Fixed64) {
                    serializer.write(com.toasttab.protokt.rt.Fixed64(l.value))
                }

                override fun write(l: SFixed64) {
                    serializer.write(com.toasttab.protokt.rt.SFixed64(l.value))
                }

                override fun write(l: UInt64) {
                    serializer.write(com.toasttab.protokt.rt.UInt64(l.value))
                }

                override fun write(l: SInt64) {
                    serializer.write(com.toasttab.protokt.rt.SInt64(l.value))
                }

                override fun write(l: Int64) {
                    serializer.write(com.toasttab.protokt.rt.Int64(l.value))
                }

                override fun write(f: Float) {
                    serializer.write(f)
                }

                override fun write(d: Double) {
                    serializer.write(d)
                }

                override fun write(s: String) {
                    serializer.write(s)
                }

                override fun write(b: Boolean) {
                    serializer.write(b)
                }

                override fun write(b: ByteArray) {
                    serializer.write(b)
                }

                override fun write(b: BytesSlice) {
                    serializer.write(com.toasttab.protokt.rt.BytesSlice(b.array, b.offset, b.length))
                }
            }
        )
    }
}
