package com.toasttab.protokt.testing.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.testing.options.CustomMarshallerServiceGrpc.customMarshallersMethodMethod
import org.junit.jupiter.api.Test

class CustomMarshallersTest {
    @Test
    fun `custom request marshaller is respected`() {
        assertThat(customMarshallersMethodMethod.requestMarshaller)
            .isInstanceOf(InMarshaller::class.java)
    }

    @Test
    fun `custom response marshaller is respected`() {
        assertThat(customMarshallersMethodMethod.responseMarshaller)
            .isInstanceOf(OutMarshaller::class.java)
    }
}
