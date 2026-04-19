/*
 * Copyright (c) 2026 Toast, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package protokt.v1.codegen.util

import com.google.protobuf.DescriptorProtos
import protokt.v1.AbstractDeserializer
import protokt.v1.Extension
import protokt.v1.Message
import protokt.v1.Reader
import protokt.v1.UnknownFieldSet
import protokt.v1.get

private fun <T> DescriptorProtos.FileOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.MessageOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.FieldOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.OneofOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.EnumOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.EnumValueOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.ServiceOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

private fun <T> DescriptorProtos.MethodOptions.protoktExt(ext: Extension<Message, T>): T? =
    deserializeAsUnknownFields(toByteArray())[ext]

internal fun DescriptorProtos.FileOptions.protoktOptions(): protokt.v1.FileOptions =
    protoktExt(ProtoktExtensions.file) ?: protokt.v1.FileOptions {}

internal fun DescriptorProtos.MessageOptions.protoktOptions(): protokt.v1.MessageOptions =
    protoktExt(ProtoktExtensions.class_) ?: protokt.v1.MessageOptions {}

internal fun DescriptorProtos.FieldOptions.protoktOptions(): protokt.v1.FieldOptions =
    protoktExt(ProtoktExtensions.property) ?: protokt.v1.FieldOptions {}

internal fun DescriptorProtos.OneofOptions.protoktOptions(): protokt.v1.OneofOptions =
    protoktExt(ProtoktExtensions.oneof) ?: protokt.v1.OneofOptions {}

internal fun DescriptorProtos.EnumOptions.protoktOptions(): protokt.v1.EnumOptions =
    protoktExt(ProtoktExtensions.enum_) ?: protokt.v1.EnumOptions {}

internal fun DescriptorProtos.EnumValueOptions.protoktOptions(): protokt.v1.EnumValueOptions =
    protoktExt(ProtoktExtensions.enumValue) ?: protokt.v1.EnumValueOptions {}

internal fun DescriptorProtos.ServiceOptions.protoktOptions(): protokt.v1.ServiceOptions =
    protoktExt(ProtoktExtensions.service) ?: protokt.v1.ServiceOptions {}

internal fun DescriptorProtos.MethodOptions.protoktOptions(): protokt.v1.MethodOptions =
    protoktExt(ProtoktExtensions.method) ?: protokt.v1.MethodOptions {}

private fun deserializeAsUnknownFields(bytes: ByteArray): UnknownFieldsMessage =
    UnknownFieldsMessage.deserialize(bytes)

private class UnknownFieldsMessage(
    override val unknownFields: UnknownFieldSet
) : Message {
    override fun serializedSize() =
        error("not used")
    override fun serialize(writer: protokt.v1.Writer) =
        error("not used")
    override fun serialize() =
        error("not used")

    companion object : AbstractDeserializer<UnknownFieldsMessage>() {
        override fun deserialize(reader: Reader): UnknownFieldsMessage {
            val unknownFields = UnknownFieldSet.Builder()
            while (true) {
                when (reader.readTag()) {
                    0u -> return UnknownFieldsMessage(UnknownFieldSet.from(unknownFields))
                    else -> unknownFields.add(reader.readUnknown())
                }
            }
        }
    }
}
