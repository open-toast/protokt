package com.toasttab.protokt.rt

expect abstract class AbstractKtDeserializer<T : KtMessage> : KtDeserializer<T> {
    override fun deserialize(bytes: Bytes): T

    override fun deserialize(bytes: ByteArray): T

    override fun deserialize(bytes: BytesSlice): T
}
