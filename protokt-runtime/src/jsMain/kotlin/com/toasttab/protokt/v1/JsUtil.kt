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

package com.toasttab.protokt.v1

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

internal fun Uint8Array.asByteArray() =
    Int8Array(buffer, byteOffset, length).unsafeCast<ByteArray>()

internal fun ByteArray.asUint8Array() =
    Uint8Array(
        unsafeCast<Int8Array>().buffer,
        unsafeCast<Int8Array>().byteOffset,
        unsafeCast<Int8Array>().length,
    )

internal fun BytesSlice.asUint8Array() =
    Uint8Array(
        array.unsafeCast<Int8Array>().buffer,
        offset,
        length,
    )

internal fun protobufjsLong(@Suppress("UNUSED_PARAMETER") long: Long): dynamic {
    // debug:
    // what you're given:
    // printErr("converting long " + long)
    // printErr("obj: " + js("JSON.stringify(long)"))

    // what protobufjs thinks you've given it:
    // js("var protobuf = require(\"protobufjs/light\")")
    // js("var LongBits = protobuf.util.LongBits")
    // js("var obj = LongBits.from(long)")
    // printErr("obj: " + js("JSON.stringify(obj)"))

    // conformance tests expect g4_1/h4_1 in node; normal kotlin expects low_1/high_1 in both browser and node
    val ret = js("{}")
    ret.low = js("long.low_1 || long.g4_1")
    ret.high = js("long.high_1 || long.h4_1")
    return ret
}

internal fun Long.Companion.fromProtobufJsLong(l: dynamic): Long {
    return if (l.low == null || l.high == null) {
        (l as Int).toLong()
    } else {
        val low = l.low as Int
        val high = l.high as Int
        return (high.toLong() shl 32) + (low.toLong() and 0xffffffffL)
    }
}

fun configureLong() {
    if (!configuredLong) {
        configuredLong = true
        js("var Long = require(\"long\")")
        js("var protobuf = require(\"protobufjs/light\")")
        js("protobuf.util.Long = Long")
        js("protobuf.configure()")
    }
}

private var configuredLong = false

@Suppress("UNUSED")
private fun printErr(message: String) {
    Process.stderr.write(message + "\n")
}

@JsModule("process")
@JsNonModule
private external object Process {
    val stderr: StdStream
}

private external interface StdStream {
    // Writeable streams only
    fun write(chunk: String, encoding: String = definedExternally): Boolean
}
