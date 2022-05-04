package com.toasttab.protokt.rt

import com.google.protobuf.CodedOutputStream

actual fun KtMessage.serialize(): ByteArray =
    ByteArray(messageSize).apply {
        serializer(CodedOutputStream.newInstance(this))
    }

fun serializer(stream: CodedOutputStream): KtMessageSerializer {
    return object : KtMessageSerializer {
        override fun write(i: Fixed32) =
            stream.writeFixed32NoTag(i.value)

        override fun write(i: SFixed32) =
            stream.writeSFixed32NoTag(i.value)

        override fun write(i: UInt32) =
            stream.writeUInt32NoTag(i.value)

        override fun write(i: SInt32) =
            stream.writeSInt32NoTag(i.value)

        override fun write(i: Int32) =
            stream.writeInt32NoTag(i.value)

        override fun write(l: Fixed64) =
            stream.writeFixed64NoTag(l.value)

        override fun write(l: SFixed64) =
            stream.writeSFixed64NoTag(l.value)

        override fun write(l: UInt64) =
            stream.writeUInt64NoTag(l.value)

        override fun write(l: SInt64) =
            stream.writeSInt64NoTag(l.value)

        override fun write(l: Int64) =
            stream.writeInt64NoTag(l.value)

        override fun write(b: Boolean) =
            stream.writeBoolNoTag(b)

        override fun write(s: String) =
            stream.writeStringNoTag(s)

        override fun write(f: Float) =
            stream.writeFloatNoTag(f)

        override fun write(d: Double) =
            stream.writeDoubleNoTag(d)

        override fun write(b: ByteArray) =
            stream.writeByteArrayNoTag(b)

        override fun write(e: KtEnum) =
            stream.writeInt32NoTag(e.value)

        override fun write(m: KtMessage) {
            stream.writeUInt32NoTag(m.messageSize)
            m.serialize(this)
        }

        override fun write(b: BytesSlice) {
            stream.writeUInt32NoTag(b.length)
            stream.write(b.array, b.offset, b.length)
        }

        override fun writeUnknown(u: UnknownFieldSet) {
            u.unknownFields.forEach { (k, v) -> v.write(k, this) }
        }

        override fun write(t: Tag) =
            also { stream.writeUInt32NoTag(t.value) }
    }
}
