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
 * Validates that [bytes] is well-formed UTF-8. Throws [IllegalArgumentException]
 * on invalid input.
 *
 * Modeled after com.google.protobuf.Utf8.SafeProcessor: fast-path ASCII
 * scanning, direct byte-level overlong/surrogate checks (no codepoint
 * computation), continuation-byte validation via unsigned comparison.
 */
internal fun validateUtf8(bytes: ByteArray) {
    val limit = bytes.size
    var i = 0

    // Fast-path: scan ASCII bytes without per-byte branching into the
    // multi-byte `when`.  Most proto string payloads are predominantly ASCII.
    while (i < limit && bytes[i] >= 0) {
        i++
    }

    // Now handle multi-byte sequences.
    while (i < limit) {
        val b0 = bytes[i]

        if (b0 >= 0) {
            // 0xxxxxxx – ASCII
            i++
            // Resume tight ASCII scan
            while (i < limit && bytes[i] >= 0) {
                i++
            }
            continue
        }

        // Multi-byte: signed byte < 0 means high bit set.
        // Use unsigned int for range comparisons.
        val u0 = b0.toInt() and 0xFF

        when {
            // Two-byte form: 110xxxxx 10xxxxxx
            // Lead byte 0xC2..0xDF (< 0xC2 means overlong or continuation)
            u0 < 0xE0 -> {
                if (u0 < 0xC2) invalid()
                if (i + 1 >= limit) invalid()
                if (bytes[i + 1].toInt() and 0xC0 != 0x80) invalid()
                i += 2
            }

            // Three-byte form: 1110xxxx 10xxxxxx 10xxxxxx
            u0 < 0xF0 -> {
                if (i + 2 >= limit) invalid()
                val b1 = bytes[i + 1]
                val b2 = bytes[i + 2]
                // Continuation byte check
                if (b1.toInt() and 0xC0 != 0x80 || b2.toInt() and 0xC0 != 0x80) invalid()
                // Overlong check: 0xE0 requires byte2 >= 0xA0
                if (u0 == 0xE0 && b1.toInt() and 0xFF < 0xA0) invalid()
                // Surrogate rejection: 0xED requires byte2 < 0xA0
                if (u0 == 0xED && b1.toInt() and 0xFF >= 0xA0) invalid()
                i += 3
            }

            // Four-byte form: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            u0 < 0xF5 -> {
                if (i + 3 >= limit) invalid()
                val b1 = bytes[i + 1]
                val b2 = bytes[i + 2]
                val b3 = bytes[i + 3]
                // Continuation byte checks
                if (b1.toInt() and 0xC0 != 0x80 || b2.toInt() and 0xC0 != 0x80 || b3.toInt() and 0xC0 != 0x80) invalid()
                // Overlong + out-of-range check (matches protobuf-java's branchless trick):
                // 0xF0 requires byte2 >= 0x90; 0xF4 requires byte2 < 0x90
                val u1 = b1.toInt() and 0xFF
                if (u0 == 0xF0 && u1 < 0x90) invalid()
                if (u0 == 0xF4 && u1 >= 0x90) invalid()
                i += 4
            }

            // 0xF5..0xFF are invalid lead bytes
            else -> invalid()
        }
    }
}

private fun invalid(): Nothing =
    throw IllegalArgumentException("Invalid UTF-8")

/**
 * Returns the number of bytes needed to encode [s] as UTF-8,
 * without allocating any intermediate objects.
 *
 * Modeled after com.google.protobuf.Utf8.encodedLength: ASCII fast-scan,
 * branch-free accounting for chars < 0x800, general fallback for BMP+.
 */
internal fun utf8Length(s: String): Int {
    val utf16Length = s.length
    var utf8Length = utf16Length
    var i = 0

    // Fast-scan ASCII: each char is exactly 1 UTF-8 byte.
    while (i < utf16Length && s[i].code < 0x80) {
        i++
    }

    // Mixed content: branch-free for chars < 0x800.
    // Start with utf8Length = utf16Length (1 byte assumed per char);
    // add 1 extra byte per non-ASCII char (branch-free via unsigned shift).
    while (i < utf16Length) {
        val c = s[i]
        if (c.code < 0x800) {
            // Branch-free: adds 0 for ASCII, 1 for 0x80..0x7FF
            utf8Length += (0x7f - c.code) ushr 31
        } else {
            utf8Length += utf8LengthGeneral(s, i)
            break
        }
        i++
    }

    return utf8Length
}

private fun utf8LengthGeneral(s: String, start: Int): Int {
    val utf16Length = s.length
    var extra = 0
    var i = start
    while (i < utf16Length) {
        val c = s[i]
        if (c.code < 0x800) {
            // Branch-free: adds 0 for ASCII, 1 for 0x80..0x7FF
            extra += (0x7f - c.code) ushr 31
        } else {
            // 3-byte BMP char: 2 extra bytes beyond the 1 already counted
            extra += 2
            if (c.isHighSurrogate()) {
                // Surrogate pair: 4 UTF-8 bytes. Base already counts 1 per
                // char (2 total); we added 2 extra above (= 4). Skip the
                // low surrogate so it doesn't get a spurious +2.
                i++
            }
        }
        i++
    }
    return extra
}
