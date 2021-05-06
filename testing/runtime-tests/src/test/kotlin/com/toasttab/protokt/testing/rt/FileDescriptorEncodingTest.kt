/*
 * Copyright (c) 2021 Toast Inc.
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

package com.toasttab.protokt.testing.rt

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.GeneratedMessageV3
import com.toasttab.protokt.Api
import com.toasttab.protokt.Descriptor
import com.toasttab.protokt.DescriptorProto
import com.toasttab.protokt.FileDescriptor
import com.toasttab.protokt.FileDescriptorProto
import com.toasttab.protokt.Type
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.testing.rt.DeeplyNested1.DeeplyNested2.DeeplyNested3.DeeplyNested4
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import org.junit.jupiter.api.Test

// Assert against a sampling of generated descriptors.
class FileDescriptorEncodingTest {
    @Test
    fun `encoding of file descriptors is equal`() {
        assertFileDescriptorsAreEqual(com.toasttab.protokt.Any, com.google.protobuf.Any::class)
        assertFileDescriptorsAreEqual(Api, com.google.protobuf.Api::class)
        assertFileDescriptorsAreEqual(FileDescriptorProto, DescriptorProtos.FileDescriptorProto::class)
        assertFileDescriptorsAreEqual(Type, com.google.protobuf.Type::class)

        // nested types
        assertFileDescriptorsAreEqual(DescriptorProto.ExtensionRange, DescriptorProtos.DescriptorProto.ExtensionRange::class)
        assertFileDescriptorsAreEqual(DeeplyNested4, DeeplyNested.DeeplyNested1.DeeplyNested2.DeeplyNested3.DeeplyNested4::class)

        // todo: get a really big type descriptor that doesn't fit in one string

        fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
            Descriptors.FileDescriptor.buildFrom(
                DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
                dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
                true
            )

        Api.descriptor.file.toProtobufJavaDescriptor()

        assertThat(Api.descriptor.file.toProtobufJavaDescriptor().dependencies.map { it.toProto() })
            .isEqualTo(com.google.protobuf.Api.getDescriptor().file.dependencies.map { it.toProto() })
    }

    private fun assertFileDescriptorsAreEqual(
        protokt: KtDeserializer<*>,
        google: KClass<out GeneratedMessageV3>
    ) {
        assertThat(
            protokt.descriptor().proto
        ).isEqualTo(
            FileDescriptorProto.deserialize(google.descriptor().toProto().toByteArray())
        )

        assertThat(
            DescriptorProtos.FileDescriptorProto.parseFrom(
                protokt.descriptor().proto.serialize()
            )
        ).isEqualTo(google.descriptor().toProto())
    }

    @Test
    fun `check re-encoding to protobuf-java FDP`() {
        assertEqualComponents(com.toasttab.protokt.Any, com.google.protobuf.Any::class)
        assertEqualComponents(Api, com.google.protobuf.Api::class)
        assertEqualComponents(FileDescriptorProto, DescriptorProtos.FileDescriptorProto::class)
        assertEqualComponents(Type, com.google.protobuf.Type::class)

        // nested types
        assertEqualComponents(DescriptorProto.ExtensionRange, DescriptorProtos.DescriptorProto.ExtensionRange::class)
        assertEqualComponents(DeeplyNested4, DeeplyNested.DeeplyNested1.DeeplyNested2.DeeplyNested3.DeeplyNested4::class)
    }

    fun assertEqualComponents(
        protokt: KtDeserializer<*>,
        google: KClass<out GeneratedMessageV3>
    ) {
        val asProtoDesc = protokt.descriptor().toProtobufJavaDescriptor()

        assertThat(asProtoDesc.toProto())
            .isEqualTo(google.descriptor().toProto())

        assertThat(asProtoDesc.messageTypes.map { it.toProto() })
            .isEqualTo(google.descriptor().messageTypes.map { it.toProto() })

        assertThat(asProtoDesc.enumTypes.map { it.toProto() })
            .isEqualTo(google.descriptor().enumTypes.map { it.toProto() })

        assertThat(asProtoDesc.services.map { it.toProto() })
            .isEqualTo(google.descriptor().services.map { it.toProto() })

        assertThat(asProtoDesc.extensions.map { it.toProto() })
            .isEqualTo(google.descriptor().extensions.map { it.toProto() })

        assertThat(asProtoDesc.dependencies.map { it.toProto() })
            .isEqualTo(google.descriptor().dependencies.map { it.toProto() })

        assertThat(asProtoDesc.publicDependencies.map { it.toProto() })
            .isEqualTo(google.descriptor().publicDependencies.map { it.toProto() })
    }

    fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
        Descriptors.FileDescriptor.buildFrom(
            DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
            dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
            true
        )

    fun KtDeserializer<*>.descriptor() =
        this::class
            .propertyNamed("descriptor")
            .let {
                @Suppress("UNCHECKED_CAST")
                it as KProperty1<Any, Descriptor>
            }
            .get(this)
            .file

    fun KClass<out GeneratedMessageV3>.descriptor() =
        java.getMethod("getDescriptor")
            .invoke(null)
            .let { it as Descriptors.Descriptor }
            .file
}
