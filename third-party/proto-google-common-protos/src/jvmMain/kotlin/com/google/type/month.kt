@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/type/month.proto
package com.google.type

import com.toasttab.protokt.EnumDescriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
object MonthProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/type/month.protogoogle.type*ﾰ" +
                    "\nMonth\nMONTH_UNSPECIFIED \nJANUA" +
                    "RY\n\bFEBRUARY\t\nMARCH\t\nAPRIL" +
                    "\nMAY\b\nJUNE\b\nJULY\n\nAUGUST" +
                    "\b\r\n\tSEPTEMBER\t\nOCTOBER\n\n\bNOVEMBE" +
                    "R\n\bDECEMBERB]\ncom.google.typeB\nMo" +
                    "nthProtoPZ6google.golang.org/genproto/g" +
                    "oogleapis/type/month;monthﾢGTPbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

val Month.Deserializer.descriptor: EnumDescriptor
    get() = MonthProto.descriptor.enumTypes[0]
