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
        val arr = google_protobuf_any.descriptorData.single().split(",").map { it.toByte() }
            .toByteArray()

        assertThat(
            FileDescriptorProto.deserialize(arr)
        ).isEqualTo(
            FileDescriptorProto.deserialize(
                Any
                    .getDescriptor()
                    .file
                    .toProto()
                    .toByteArray()
            )
        )
    }
}
