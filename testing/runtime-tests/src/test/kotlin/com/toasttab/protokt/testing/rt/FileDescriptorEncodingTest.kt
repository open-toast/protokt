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
import com.toasttab.protokt.FileDescriptorProto
import com.toasttab.protokt.google_protobuf_descriptor
import org.junit.jupiter.api.Test

class FileDescriptorEncodingTest {
    @Test
    fun `encoding of file descriptors is equal`() {
        assertThat(
            google_protobuf_descriptor.descriptor
        ).isEqualTo(
            FileDescriptorProto.deserialize(
                DescriptorProtos.FileDescriptorProto
                    .getDescriptor()
                    .file
                    .toProto()
                    .toByteArray()
            )
        )

        assertThat(
            DescriptorProtos.FileDescriptorProto.parseFrom(
                google_protobuf_descriptor.descriptor.serialize()
            )
        ).isEqualTo(
            DescriptorProtos.FileDescriptorProto
                .getDescriptor()
                .file
                .toProto()
        )
    }
}
