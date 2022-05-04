package com.toasttab.protokt.rt

import com.google.protobuf.CodedOutputStream
import java.io.OutputStream

fun KtMessage.serialize(outputStream: OutputStream) =
    CodedOutputStream.newInstance(outputStream).run {
        serialize(serializer(this))
        flush()
    }

fun KtMessage.serialize() =
    ByteArray(messageSize).apply {
        serialize(serializer(CodedOutputStream.newInstance(this)))
    }
