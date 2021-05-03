package com.toasttab.protokt.testing.rt

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.Any
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.compiler.PluginProtos
import com.toasttab.protokt.FileDescriptorProto
import com.toasttab.protokt.compiler.google_protobuf_compiler_plugin
import com.toasttab.protokt.google_protobuf_any
import com.toasttab.protokt.google_protobuf_descriptor
import org.junit.jupiter.api.Test

class FileDescriptorEncodingTest {
    @Test
    fun `encoding of file descriptors is equal`() {
        val arr0 = google_protobuf_descriptor.descriptorData[0]
            .split(",")
            .map { it.toByte() }
            .also { println(it) }
            .also { println(it.map { it.toChar() }) }
            .toByteArray()
            .also { println(it.size) }


        assertThat(
            FileDescriptorProto.deserialize(arr0)
        ).isEqualTo(
            FileDescriptorProto.deserialize(
                DescriptorProtos.FileDescriptorProto
                    .getDescriptor()
                    .file
                    .toProto()
                    .toByteArray()
            )
        )

        val data = google_protobuf_descriptor.descriptorData[1]
        val arr = ByteArray(data.length)
        data.forEachIndexed { idx, char -> arr[idx] = char.toByte() }

        assertThat(arr.asList()).isEqualTo(arr0.asList())
    }
}
