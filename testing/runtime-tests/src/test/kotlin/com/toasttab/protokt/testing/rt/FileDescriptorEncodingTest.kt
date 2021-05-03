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
import com.toasttab.protokt.FileDescriptorProto
import com.toasttab.protokt.Type
import com.toasttab.protokt.rt.KtDeserializer
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class FileDescriptorEncodingTest {
    // Assert against a sampling of generated descriptors.
    @Test
    fun `encoding of file descriptors is equal`() {
        assertDescriptorsAreEqual(com.toasttab.protokt.Any, com.google.protobuf.Any::class)
        assertDescriptorsAreEqual(Api, com.google.protobuf.Api::class)
        assertDescriptorsAreEqual(FileDescriptorProto, DescriptorProtos.FileDescriptorProto::class)
        assertDescriptorsAreEqual(Type, com.google.protobuf.Type::class)

        // todo: get a really big type descriptor that doesn't fit in one string
    }

    private fun assertDescriptorsAreEqual(
        protokt: KtDeserializer<*>,
        googleMessageClass: KClass<out GeneratedMessageV3>
    ) {
        val googleDescriptor =
            googleMessageClass.java.getMethod("getDescriptor")
                .invoke(null)
                .let { it as Descriptors.Descriptor }
                .file
                .toProto()

        val protoktDescriptor =
            protokt::class
                .propertyNamed("fileDescriptor")
                .let {
                    @Suppress("UNCHECKED_CAST")
                    it as KProperty1<Any, FileDescriptorProto>
                }
                .get(protokt)

        assertThat(
            protoktDescriptor
        ).isEqualTo(
            FileDescriptorProto.deserialize(googleDescriptor.toByteArray())
        )

        assertThat(
            DescriptorProtos.FileDescriptorProto.parseFrom(
                protoktDescriptor.serialize()
            )
        ).isEqualTo(googleDescriptor)
    }
}
