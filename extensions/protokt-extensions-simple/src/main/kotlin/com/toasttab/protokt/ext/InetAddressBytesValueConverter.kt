package com.toasttab.protokt.ext

import com.google.auto.service.AutoService
import com.toasttab.protokt.BytesValue
import com.toasttab.protokt.rt.Bytes
import java.net.InetAddress

@AutoService(Converter::class)
object InetAddressBytesValueConverter : Converter<InetAddress, BytesValue> {
    override val wrapper = InetAddress::class

    override val wrapped = BytesValue::class

    override fun wrap(unwrapped: BytesValue) =
        InetAddressConverter.wrap(unwrapped.value.bytes)

    override fun unwrap(wrapped: InetAddress) =
        BytesValue { value = Bytes(InetAddressConverter.unwrap(wrapped)) }
}
