/*
 * Copyright (c) 2026 Toast, Inc.
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

/**
 * Thrown when protobuf input is malformed or cannot be fully decoded.
 */
class ProtoktDecodeException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}

internal inline fun protoktCheck(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        throw ProtoktDecodeException(lazyMessage().toString())
    }
}

internal inline fun protoktRequire(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        throw ProtoktDecodeException(lazyMessage().toString())
    }
}

internal inline fun <T> decode(block: () -> T): T =
    try {
        block()
    } catch (e: ProtoktDecodeException) {
        throw e
    } catch (e: Throwable) {
        val message = e.message ?: "Malformed protobuf input"
        val normalizedMessage = if (message.contains("malformed varint", ignoreCase = true)) WireFormat.MALFORMED_VARINT else message
        throw ProtoktDecodeException(normalizedMessage, e)
    }
