@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/timeofday.proto
package com.google.type

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object TimeOfDayProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/type/timeofday.protogoogle.ty" +
                    "pe\"K\n\tTimeOfDay\r\nhours (\nminute" +
                    "s (\nseconds (\r\nnanos (B" +
                    "l\ncom.google.typeBTimeOfDayProtoPZ>go" +
                    "ogle.golang.org/genproto/googleapis/type" +
                    "/timeofday;timeofday￸ﾢGTPbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

@Deprecated("use v1")
val TimeOfDay.Deserializer.descriptor: Descriptor
    get() = TimeOfDayProto.descriptor.messageTypes[0]
