package com.toasttab.protokt.rt

expect abstract class AbstractKtMessage : KtMessage {
    override fun serialize(): ByteArray
}
