@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/dayofweek.proto
package com.google.type

import com.toasttab.protokt.EnumDescriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object DayOfWeekProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/type/dayofweek.protogoogle.ty" +
                    "pe*ﾄ\n\tDayOfWeek\nDAY_OF_WEEK_UNSPECIF" +
                    "IED \n\nMONDAY\nTUESDAY\r\n\tWEDNES" +
                    "DAY\n\bTHURSDAY\n\nFRIDAY\n\bSATUR" +
                    "DAY\n\nSUNDAYBi\ncom.google.typeBDa" +
                    "yOfWeekProtoPZ>google.golang.org/genpro" +
                    "to/googleapis/type/dayofweek;dayofweekﾢ" +
                    "GTPbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

@Deprecated("use v1")
val DayOfWeek.Deserializer.descriptor: EnumDescriptor
    get() = DayOfWeekProto.descriptor.enumTypes[0]
