package com.toasttab.protokt.codegen.impl

import com.google.protobuf.DescriptorProtos

private const val BYTES_PER_LINE = 40
private const val LINES_PER_PART = 400
private const val BYTES_PER_PART = BYTES_PER_LINE * LINES_PER_PART

// see https://github.com/protocolbuffers/protobuf/blob/5e84a6169cf0f9716c9285c95c860bcb355dbdc1/src/google/protobuf/compiler/java/java_shared_code_generator.cc#L119
// and https://github.com/protocolbuffers/protobuf/blob/5e84a6169cf0f9716c9285c95c860bcb355dbdc1/src/google/protobuf/stubs/strutil.cc#L595
fun encodeFileDescriptor(fileDescriptorProto: DescriptorProtos.FileDescriptorProto): List<List<String>> {
    val parts = mutableListOf<MutableList<String>>()
    val bytes = fileDescriptorProto.toByteArray()
    for (i in bytes.indices step BYTES_PER_LINE) {
        if (i % BYTES_PER_PART == 0) {
            parts.add(mutableListOf())
        }
        parts.last().add(escape(bytes.drop(i).take(BYTES_PER_LINE)))
    }
    return listOf(listOf(bytes.joinToString(separator = ",") { it.toString() }))
}

private fun escape(bytes: List<Byte>) =
    bytes.joinToString(separator = "") {
        when (it.toChar()) {
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\t' -> "\\t"
            '\"' -> "\\\""
            '\'' -> "\\'"
            '\\' -> "\\\\"
            '\b' -> "\\b"
            '$' -> "\\\$"
            else -> "\\u" + it.toUByte().toString(16).padStart(4, '0')
        }
    }
