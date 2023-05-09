package com.toasttab.protokt.testing.rt

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.grpc.SchemaDescriptor
import com.toasttab.protokt.grpc.fileDescriptor
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.rt.TestServiceGrpc

class SchemaDescriptorTest {
    @Test
    fun `schemadescriptor has correct file descriptor`() {
        val schemaDescriptor = TestServiceGrpc.getServiceDescriptor().schemaDescriptor as SchemaDescriptor
        assertThat(schemaDescriptor.fileDescriptor.proto)
            .isEqualTo(toasttab.protokt.testing.rt.ServicePackage.descriptor.proto)
    }
}
