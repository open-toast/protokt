package com.toasttab.protokt.rt

import com.google.protobuf.CodedInputStream
import java.io.InputStream
import java.nio.ByteBuffer

fun <T : KtMessage> KtDeserializer<T>.deserialize(bytes: BytesSlice) =
    deserialize(
        deserializer(
            CodedInputStream.newInstance(
                bytes.array,
                bytes.offset,
                bytes.length
            )
        )
    )

fun <T : KtMessage> KtDeserializer<T>.deserialize(stream: InputStream) =
    deserialize(deserializer(CodedInputStream.newInstance(stream)))

fun <T : KtMessage> KtDeserializer<T>.deserialize(buffer: ByteBuffer) =
    deserialize(deserializer(CodedInputStream.newInstance(buffer)))
