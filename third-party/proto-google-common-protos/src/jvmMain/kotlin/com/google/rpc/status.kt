@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/rpc/status.proto
package com.google.rpc

import com.toasttab.protokt.AnyProto
import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object StatusProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/rpc/status.proto\ngoogle.rpcg" +
                    "oogle/protobuf/any.proto\"N\nStatus\nco" +
                    "de (\nmessage (\t%\ndetails " +
                    "(2.google.protobuf.AnyBa\ncom.google.r" +
                    "pcBStatusProtoPZ7google.golang.org/gen" +
                    "proto/googleapis/rpc/status;status￸ﾢ" +
                    "RPCbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        AnyProto.descriptor
                    )
                )
            }
}

@Deprecated("use v1")
val Status.Deserializer.descriptor: Descriptor
    get() = StatusProto.descriptor.messageTypes[0]
