@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/cloud/extended_operations.proto
package com.google.cloud

import com.toasttab.protokt.DescriptorProtos
import com.toasttab.protokt.EnumDescriptor
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

@Deprecated("use v1")
@KtGeneratedFileDescriptor
object ExtendedOperationsProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\n&google/cloud/extended_operations.proto" +
                    "google.cloud google/protobuf/descript" +
                    "or.proto*b\nOperationResponseMapping\r\n\t" +
                    "UNDEFINED \b\nNAME\n\nSTATUS\n\nERR" +
                    "OR_CODE\n\rERROR_MESSAGE:o\noperatio" +
                    "n_field.google.protobuf.FieldOptions�" +
                    "\b (2&.google.cloud.OperationResponseMa" +
                    "ppingRoperationField:V\noperation_reque" +
                    "st_field.google.protobuf.FieldOptions" +
                    "￾\b (\tRoperationRequestField:X\noperati" +
                    "on_response_field.google.protobuf.Fiel" +
                    "dOptions￿\b (\tRoperationResponseField:" +
                    "L\noperation_service.google.protobuf.M" +
                    "ethodOptions￡\t (\tRoperationService:Y\n" +
                    "operation_polling_method.google.proto" +
                    "buf.MethodOptions￢\t (\bRoperationPolli" +
                    "ngMethodBy\ncom.google.cloudBExtendedOp" +
                    "erationsProtoPZCgoogle.golang.org/genpr" +
                    "oto/googleapis/cloud/extendedops;extende" +
                    "dopsﾢGAPIbproto3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        DescriptorProtos.descriptor
                    )
                )
            }
}

@Deprecated("use v1")
val OperationResponseMapping.Deserializer.descriptor: EnumDescriptor
    get() = ExtendedOperationsProto.descriptor.enumTypes[0]
