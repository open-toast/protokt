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

@file:JsModule("protobufjs/light")
@file:JsNonModule

package protokt.v1

import org.khronos.webgl.Uint8Array

internal external class Writer {
    fun bool(value: Boolean): Writer

    fun bytes(value: Uint8Array): Writer

    fun double(value: Double): Writer

    fun fixed32(value: Int): Writer

    fun fixed64(value: dynamic): Writer

    fun float(value: Float): Writer

    fun int32(value: Int): Writer

    fun int64(value: dynamic): Writer

    fun sfixed32(value: Int): Writer

    fun sfixed64(value: dynamic): Writer

    fun sint32(value: Int): Writer

    fun sint64(value: dynamic): Writer

    fun string(value: String): Writer

    fun uint32(value: Int): Writer

    fun uint64(value: dynamic): Writer

    fun finish(): Uint8Array

    companion object {
        fun create(): Writer
    }
}

internal external class Reader {
    val len: Int

    val pos: Int

    fun bytes(): Uint8Array

    fun double(): Double

    fun fixed32(): Int

    fun fixed64(): dynamic

    fun float(): Float

    fun int64(): dynamic

    fun sfixed32(): Int

    fun sfixed64(): dynamic

    fun sint32(): Int

    fun sint64(): dynamic

    fun string(): String

    fun uint64(): dynamic

    companion object {
        fun create(buf: Uint8Array): Reader
    }
}
