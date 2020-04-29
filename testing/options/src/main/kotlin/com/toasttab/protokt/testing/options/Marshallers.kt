package com.toasttab.protokt.testing.options

import io.grpc.MethodDescriptor
import java.io.InputStream

object InMarshaller : MethodDescriptor.Marshaller<In> {
    override fun stream(value: In) =
        value.serialize().inputStream()

    override fun parse(stream: InputStream) =
        In.deserialize(stream)
}

object OutMarshaller : MethodDescriptor.Marshaller<Out> {
    override fun stream(value: Out) =
        value.serialize().inputStream()

    override fun parse(stream: InputStream) =
        Out.deserialize(stream)
}
