@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: protokt/ext/inet_socket_address.proto
package com.toasttab.protokt.ext

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
@Deprecated("use v1")
object InetSocketAddressProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\n%protokt/ext/inet_socket_address.proto" +
                    "protokt.extprotokt/protokt.proto\"M\nI" +
                    "netSocketAddress*\naddress (BﾂC" +
                    "java.net.InetAddress\nport (Bg\nco" +
                    "m.toasttab.protokt.extBInetSocketAddres" +
                    "sProtoﾂC2\ncom.toasttab.protokt.extIne" +
                    "tSocketAddressProtobproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        ProtoktProto.descriptor
                    )
                )
            }
}

val InetSocketAddress.Deserializer.descriptor: Descriptor
    get() = InetSocketAddressProto.descriptor.messageTypes[0]
