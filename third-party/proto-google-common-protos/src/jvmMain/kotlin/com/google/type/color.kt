@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/color.proto
package com.google.type

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.WrappersProto
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object ColorProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/type/color.protogoogle.type" +
                    "google/protobuf/wrappers.proto\"]\nColor" +
                    "\nred (\r\ngreen (\nblue (" +
                    "*\nalpha (2.google.protobuf.Float" +
                    "ValueB`\ncom.google.typeB\nColorProtoPZ6" +
                    "google.golang.org/genproto/googleapis/ty" +
                    "pe/color;color￸ﾢGTPbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        WrappersProto.descriptor
                    )
                )
            }
}

@Deprecated("use v1")
val Color.Deserializer.descriptor: Descriptor
    get() = ColorProto.descriptor.messageTypes[0]
