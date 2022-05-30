package com.toasttab.protokt.testing.options

import com.google.common.truth.Truth.assertThat
import com.toasttab.protokt.testing.options.CustomMarshallerServiceGrpc.getCustomMarshallersMethodMethod
import org.junit.jupiter.api.Test

class CustomMarshallersTest {
    @Test
    fun `custom request marshaller is respected`() {
        assertThat(getCustomMarshallersMethodMethod().requestMarshaller)
            .isInstanceOf(InMarshaller::class.java)
    }

    @Test
    fun `custom response marshaller is respected`() {
        assertThat(getCustomMarshallersMethodMethod().responseMarshaller)
            .isInstanceOf(OutMarshaller::class.java)
    }
}
