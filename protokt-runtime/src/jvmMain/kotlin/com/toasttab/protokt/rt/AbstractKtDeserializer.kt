package com.toasttab.protokt.rt

import com.google.protobuf.CodedInputStream

actual abstract class AbstractKtDeserializer<T : KtMessage> : KtDeserializer<T> {
    actual override fun deserialize(bytes: Bytes) =
        deserialize(bytes.value)

    actual override fun deserialize(bytes: ByteArray) =
        deserialize(deserializer(CodedInputStream.newInstance(bytes), bytes))

    actual override fun deserialize(bytes: BytesSlice) =
        deserialize(
            deserializer(
                CodedInputStream.newInstance(
                    bytes.array,
                    bytes.offset,
                    bytes.length
                )
            )
        )
}
