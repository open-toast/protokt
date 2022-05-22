/*
 * Copyright (c) 2022 Toast Inc.
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

@Suppress("UNUSED_PARAMETER")
fun serializer(bytes: ByteArray): KtMessageSerializer {
    return object : KtMessageSerializer {
        override fun write(i: Fixed32) =
            TODO()

        override fun write(i: SFixed32) =
            TODO()

        override fun write(i: UInt32) =
            TODO()

        override fun write(i: SInt32) =
            TODO()

        override fun write(i: Int32) =
            TODO()

        override fun write(l: Fixed64) =
            TODO()

        override fun write(l: SFixed64) =
            TODO()

        override fun write(l: UInt64) =
            TODO()

        override fun write(l: SInt64) =
            TODO()

        override fun write(l: Int64) =
            TODO()

        override fun write(b: Boolean) =
            TODO()

        override fun write(s: String) =
            TODO()

        override fun write(f: Float) =
            TODO()

        override fun write(d: Double) =
            TODO()

        override fun write(b: ByteArray) =
            TODO()

        override fun write(e: KtEnum) =
            TODO()

        override fun write(m: KtMessage) {
            TODO()
        }

        override fun write(b: BytesSlice) {
            TODO()
        }

        override fun writeUnknown(u: UnknownFieldSet) {
            TODO()
        }

        override fun write(t: Tag) =
            TODO()
    }
}
