@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/log.proto
package com.google.api

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
object LogProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/api/log.proto\ngoogle.apigoog" +
                    "le/api/label.proto\"u\n\rLogDescriptor\nn" +
                    "ame (\t+\nlabels (2.google.api.L" +
                    "abelDescriptor\ndescription (\t\nd" +
                    "isplay_name (\tBj\ncom.google.apiB\bLog" +
                    "ProtoPZEgoogle.golang.org/genproto/goog" +
                    "leapis/api/serviceconfig;serviceconfigﾢ" +
                    "GAPIbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        LabelProto.descriptor
                    )
                )
            }
}

val LogDescriptor.Deserializer.descriptor: Descriptor
    get() = LogProto.descriptor.messageTypes[0]
