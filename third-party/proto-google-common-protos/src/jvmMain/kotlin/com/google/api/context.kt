@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/context.proto
package com.google.api

import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@KtGeneratedFileDescriptor
object ContextProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\ngoogle/api/context.proto\ngoogle.api\"1" +
                    "\nContext&\nrules (2.google.api.Co" +
                    "ntextRule\"ﾍ\nContextRule\n\bselector " +
                    "(\t\n\trequested (\t\n\bprovided (\t" +
                    "\"\nallowed_request_extensions (\t#\n" +
                    "allowed_response_extensions (\tBn\ncom" +
                    ".google.apiBContextProtoPZEgoogle.gola" +
                    "ng.org/genproto/googleapis/api/serviceco" +
                    "nfig;serviceconfigﾢGAPIbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(

                    )
                )
            }
}

val Context.Deserializer.descriptor: Descriptor
    get() = ContextProto.descriptor.messageTypes[0]

val ContextRule.Deserializer.descriptor: Descriptor
    get() = ContextProto.descriptor.messageTypes[1]
