@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/quota.proto
package com.google.api

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
object QuotaProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/api/quota.proto\ngoogle.api\"]\n" +
                    "Quota&\nlimits (2.google.api.Quota" +
                    "Limit,\nmetric_rules (2.google.api" +
                    ".MetricRule\"ﾑ\n\nMetricRule\n\bselector" +
                    " (\t=\nmetric_costs (2\'.google.api." +
                    "MetricRule.MetricCostsEntry2\nMetricCos" +
                    "tsEntry\nkey (\t\r\nvalue (:8\"" +
                    "ﾕ\n\nQuotaLimit\nname (\t\ndescript" +
                    "ion (\t\n\rdefault_limit (\n\tmax_" +
                    "limit (\n\tfree_tier (\n\bdurati" +
                    "on (\t\nmetric\b (\t\nunit\t (\t2" +
                    "\nvalues\n (2\".google.api.QuotaLimit.V" +
                    "aluesEntry\ndisplay_name (\t-\nValu" +
                    "esEntry\nkey (\t\r\nvalue (:8B" +
                    "l\ncom.google.apiB\nQuotaProtoPZEgoogle." +
                    "golang.org/genproto/googleapis/api/servi" +
                    "ceconfig;serviceconfigﾢGAPIbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

val Quota.Deserializer.descriptor: Descriptor
    get() = QuotaProto.descriptor.messageTypes[0]

val MetricRule.Deserializer.descriptor: Descriptor
    get() = QuotaProto.descriptor.messageTypes[1]

val QuotaLimit.Deserializer.descriptor: Descriptor
    get() = QuotaProto.descriptor.messageTypes[2]
