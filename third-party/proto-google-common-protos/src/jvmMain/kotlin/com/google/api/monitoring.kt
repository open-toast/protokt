@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/monitoring.proto
package com.google.api

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object MonitoringProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/api/monitoring.proto\ngoogle.ap" +
                    "i\"￬\n\nMonitoringK\nproducer_destination" +
                    "s (2,.google.api.Monitoring.Monitori" +
                    "ngDestinationK\nconsumer_destinations" +
                    " (2,.google.api.Monitoring.MonitoringD" +
                    "estinationD\nMonitoringDestination\nm" +
                    "onitored_resource (\t\nmetrics (\t" +
                    "Bq\ncom.google.apiBMonitoringProtoPZEg" +
                    "oogle.golang.org/genproto/googleapis/api" +
                    "/serviceconfig;serviceconfigﾢGAPIbpro" +
                    "to3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

@Deprecated("use v1")
val Monitoring.Deserializer.descriptor: Descriptor
    get() = MonitoringProto.descriptor.messageTypes[0]

@Deprecated("use v1")
val Monitoring.MonitoringDestination.Deserializer.descriptor: Descriptor
    get() = Monitoring.descriptor.nestedTypes[0]
