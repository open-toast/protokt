/*
 * Copyright (c) 2021 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.google.protobuf.DescriptorProtos.FileDescriptorProto

// For the Java implementation of descriptor encoding, see:
// https://github.com/protocolbuffers/protobuf/blob/5e84a6169cf0f9716c9285c95c860bcb355dbdc1/src/google/protobuf/compiler/java/java_shared_code_generator.cc#L119
// and
// https://github.com/protocolbuffers/protobuf/blob/5e84a6169cf0f9716c9285c95c860bcb355dbdc1/src/google/protobuf/stubs/strutil.cc#L595

// Limit the number of bytes per line.
private const val BYTES_PER_LINE = 40

// Limit the number of lines per string part.
private const val LINES_PER_PART = 400

// Every block of bytes, start a new string literal, in order to avoid the
// 64k length limit. Note that this value needs to be <64k.
private const val BYTES_PER_PART = BYTES_PER_LINE * LINES_PER_PART

// Notes from the Java version:
// Embed the descriptor.  We simply serialize the entire FileDescriptorProto
// and embed it as a string literal, which is parsed and built into real
// descriptors at initialization time.  We unfortunately have to put it in
// a string literal, not a byte array, because apparently using a literal
// byte array causes the Java compiler to generate *instructions* to
// initialize each and every byte of the array, e.g. as if you typed:
//   b[0] = 123; b[1] = 456; b[2] = 789;
// This makes huge bytecode files and can easily hit the compiler's internal
// code size limits (error "code to large").  String literals are apparently
// embedded raw, which is what we want.
fun encodeFileDescriptor(fileDescriptorProto: FileDescriptorProto): List<List<String>> {
    val parts = mutableListOf<MutableList<String>>()
    val bytes = fileDescriptorProto.toByteArray()
    for (i in bytes.indices step BYTES_PER_LINE) {
        if (i % BYTES_PER_PART == 0) {
            parts.add(mutableListOf())
        }
        parts.last().add(escape(bytes.asSequence().drop(i).take(BYTES_PER_LINE)))
    }
    return parts
}

private fun escape(bytes: Sequence<Byte>) =
    bytes.joinToString("") {
        when (val c = it.toInt().toChar()) {
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\t' -> "\\t"
            '\"' -> "\\\""
            '\'' -> "\\'"
            '\\' -> "\\\\"
            '\b' -> "\\b"
            '$' -> "\\\$"

            // All other characters are representable directly in Kotlin source.
            else -> c.toString()
        }
    }
