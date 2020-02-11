package com.toasttab.protokt.ext

import com.google.auto.service.AutoService
import com.toasttab.protokt.BytesValue
import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.sizeof
import java.util.UUID

@AutoService(Converter::class)
object UuidBytesValueConverter : OptimizedSizeofConverter<UUID, BytesValue> {
    override val wrapper = UUID::class

    override val wrapped = BytesValue::class

    private val sizeofProxy =
        BytesValue { value = Bytes(ByteArray(16)) }

    override fun sizeof(wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: BytesValue) =
        UuidConverter.wrap(unwrapped.value.bytes)

    override fun unwrap(wrapped: UUID) =
        BytesValue { value = Bytes(UuidConverter.unwrap(wrapped)) }
}
