@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/interval.proto
package com.google.type

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.TimestampProto
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object IntervalProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/type/interval.protogoogle.typ" +
                    "egoogle/protobuf/timestamp.proto\"h\n\bIn" +
                    "terval.\n\nstart_time (2.google.prot" +
                    "obuf.Timestamp,\n\bend_time (2.googl" +
                    "e.protobuf.TimestampBi\ncom.google.typeB" +
                    "\rIntervalProtoPZ<google.golang.org/genp" +
                    "roto/googleapis/type/interval;interval￸" +
                    "ﾢGTPbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        TimestampProto.descriptor
                    )
                )
            }
}

@Deprecated("use v1")
val Interval.Deserializer.descriptor: Descriptor
    get() = IntervalProto.descriptor.messageTypes[0]
