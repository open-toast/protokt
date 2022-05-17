package com.toasttab.protokt.rt

import com.google.protobuf.CodedOutputStream

actual abstract class AbstractKtMessage : KtMessage {
    actual override fun serialize() =
        ByteArray(messageSize).apply {
            serialize(serializer(CodedOutputStream.newInstance(this)))
        }
}
