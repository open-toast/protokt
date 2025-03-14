@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/longrunning/operations.proto
package com.google.longrunning

import com.google.rpc.StatusProto
import com.toasttab.protokt.AnyProto
import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.DescriptorProtos
import com.toasttab.protokt.DurationProto
import com.toasttab.protokt.EmptyProto
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.ServiceDescriptor
import com.toasttab.protokt.rt.KtGeneratedFileDescriptor

object Operations {
    val descriptor: ServiceDescriptor by lazy {
                OperationsProto.descriptor.services[0]
            }
}

@KtGeneratedFileDescriptor
object OperationsProto {
    val descriptor: FileDescriptor by lazy {
                val descriptorData = arrayOf(
                            "\n#google/longrunning/operations.protog" +
                    "oogle.longrunninggoogle/api/annotation" +
                    "s.protogoogle/api/client.protogoogle" +
                    "/protobuf/any.protogoogle/protobuf/dur" +
                    "ation.protogoogle/protobuf/empty.proto" +
                    "google/rpc/status.proto google/protob" +
                    "uf/descriptor.proto\"ﾨ\n\tOperation\nnam" +
                    "e (\t&\n\bmetadata (2.google.proto" +
                    "buf.Any\ndone (\b#\nerror (2.g" +
                    "oogle.rpc.StatusH (\n\bresponse (2.g" +
                    "oogle.protobuf.AnyH B\b\nresult\"#\nGetOpe" +
                    "rationRequest\nname (\t\"\\\nListOpera" +
                    "tionsRequest\nname (\t\nfilter " +
                    "(\t\n\tpage_size (\n\npage_token (" +
                    "\t\"d\nListOperationsResponse1\n\noperation" +
                    "s (2.google.longrunning.Operation" +
                    "\nnext_page_token (\t\"&\nCancelOperati" +
                    "onRequest\nname (\t\"&\nDeleteOperati" +
                    "onRequest\nname (\t\"P\nWaitOperation" +
                    "Request\nname (\t*\ntimeout (2" +
                    ".google.protobuf.Duration\"=\n\rOperationIn" +
                    "fo\n\rresponse_type (\t\n\rmetadata_ty" +
                    "pe (\t2ﾪ\n\nOperationsﾔ\nListOperatio" +
                    "ns).google.longrunning.ListOperationsRe" +
                    "quest*.google.longrunning.ListOperation" +
                    "sResponse\"+ￚAname,filterﾂￓ￤ﾓ/v1/{na" +
                    "me=operations}\nGetOperation\'.google." +
                    "longrunning.GetOperationRequest.google" +
                    ".longrunning.Operation\"\'ￚAnameﾂￓ￤ﾓ/" +
                    "v1/{name=operations/**}~\nDeleteOperati" +
                    "on*.google.longrunning.DeleteOperationR" +
                    "equest.google.protobuf.Empty\"\'ￚAnameﾂ" +
                    "ￓ￤ﾓ*/v1/{name=operations/**}ﾈ\nCanc" +
                    "elOperation*.google.longrunning.CancelO" +
                    "perationRequest.google.protobuf.Empty\"" +
                    "1ￚAnameﾂￓ￤ﾓ\$\"/v1/{name=operations/**}" +
                    ":cancel:*Z\n\rWaitOperation(.google.lon" +
                    "grunning.WaitOperationRequest.google.l" +
                    "ongrunning.Operation\" ￊAlongrunning.g" +
                    "oogleapis.com:i\noperation_info.google" +
                    ".protobuf.MethodOptionsﾙ\b (2!.google." +
                    "longrunning.OperationInfoR\roperationInfo" +
                    "Bﾗ\ncom.google.longrunningBOperationsP" +
                    "rotoPZ=google.golang.org/genproto/googl" +
                    "eapis/longrunning;longrunning￸ﾪGoogl" +
                    "e.LongRunningￊGoogle\\LongRunningbprot" +
                    "o3"
                )

                FileDescriptor.buildFrom(
                    descriptorData,
                    listOf(
                        AnyProto.descriptor,
            DurationProto.descriptor,
            EmptyProto.descriptor,
            StatusProto.descriptor,
            DescriptorProtos.descriptor
                    )
                )
            }
}

val Operation.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[0]

val GetOperationRequest.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[1]

val ListOperationsRequest.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[2]

val ListOperationsResponse.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[3]

val CancelOperationRequest.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[4]

val DeleteOperationRequest.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[5]

val WaitOperationRequest.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[6]

val OperationInfo.Deserializer.descriptor: Descriptor
    get() = OperationsProto.descriptor.messageTypes[7]
