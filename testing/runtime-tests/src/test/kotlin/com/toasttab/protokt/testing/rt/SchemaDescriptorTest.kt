package com.toasttab.protokt.testing.rt

import com.google.common.truth.Truth
import com.toasttab.protokt.grpc.SchemaDescriptor
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.rt.TestServiceGrpc

class SchemaDescriptorTest {

    @Test
    fun `schemadescriptor has correct file descriptor`() {
        val schemaDescriptor = TestServiceGrpc.getServiceDescriptor().schemaDescriptor as SchemaDescriptor
        Truth.assertThat(schemaDescriptor.fileDescriptor.proto.name)
            .isEqualTo("toasttab/protokt/testing/rt/service_package.proto")
    }
}