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

package protokt.v1.testing

import com.google.common.truth.Truth.assertThat
import com.google.protobuf.DescriptorProtos
import com.toasttab.protokt.v1.ProtoktProtos
import org.junit.jupiter.api.Test
import protokt.v1.Extension
import protokt.v1.ExtensionCodecs
import protokt.v1.RepeatedExtension
import protokt.v1.get
import protokt.v1.google.protobuf.FieldOptions

class ExtensionInteropTest {
    private val propertyExt =
        Extension<FieldOptions, protokt.v1.FieldOptions>(
            1253u,
            ExtensionCodecs.message(protokt.v1.FieldOptions)
        )

    @Test
    fun `read protokt field options from wire bytes`() {
        val javaFieldOptions =
            DescriptorProtos.FieldOptions.newBuilder()
                .setExtension(
                    ProtoktProtos.property,
                    ProtoktProtos.FieldOptions.newBuilder()
                        .setWrap("java.util.UUID")
                        .build()
                )
                .build()

        val protoktFieldOptions =
            FieldOptions.deserialize(javaFieldOptions.toByteArray())

        val opts = protoktFieldOptions[propertyExt]

        assertThat(opts).isNotNull()
        assertThat(opts!!.wrap).isEqualTo("java.util.UUID")
    }

    @Test
    fun `read protokt field options with non-null accessor`() {
        val javaFieldOptions =
            DescriptorProtos.FieldOptions.newBuilder()
                .setExtension(
                    ProtoktProtos.property,
                    ProtoktProtos.FieldOptions.newBuilder()
                        .setGenerateNonNullAccessor(true)
                        .setDeprecationMessage("old field")
                        .build()
                )
                .build()

        val protoktFieldOptions =
            FieldOptions.deserialize(javaFieldOptions.toByteArray())

        val opts = protoktFieldOptions[propertyExt]!!

        assertThat(opts.generateNonNullAccessor).isTrue()
        assertThat(opts.deprecationMessage).isEqualTo("old field")
    }

    @Test
    fun `missing extension returns null`() {
        val javaFieldOptions =
            DescriptorProtos.FieldOptions.newBuilder()
                .setDeprecated(true)
                .build()

        val protoktFieldOptions =
            FieldOptions.deserialize(javaFieldOptions.toByteArray())

        assertThat(protoktFieldOptions[propertyExt]).isNull()
    }

    @Test
    fun `read descriptor field options from live proto`() {
        val descriptor =
            com.toasttab.protokt.v1.testing.WrappersDynamic.Wrappers.getDescriptor()

        val uuidField = descriptor.findFieldByName("uuid")
        val javaOpts = uuidField.options

        val protoktOpts = FieldOptions.deserialize(javaOpts.toByteArray())
        val opts = protoktOpts[propertyExt]

        assertThat(opts).isNotNull()
        assertThat(opts!!.wrap).isEqualTo("java.util.UUID")
    }

    @Test
    fun `scalar extension round-trip`() {
        val ext = Extension<FieldOptions, Boolean>(99999u, ExtensionCodecs.bool)

        val javaFieldOptions =
            DescriptorProtos.FieldOptions.newBuilder()
                .setDeprecated(true)
                .build()

        val protoktFieldOptions =
            FieldOptions.deserialize(javaFieldOptions.toByteArray())

        assertThat(protoktFieldOptions[ext]).isNull()
    }

    @Test
    fun `repeated extension`() {
        val ext = RepeatedExtension<FieldOptions, String>(99999u, ExtensionCodecs.string)

        val javaFieldOptions =
            DescriptorProtos.FieldOptions.newBuilder().build()

        val protoktFieldOptions =
            FieldOptions.deserialize(javaFieldOptions.toByteArray())

        assertThat(protoktFieldOptions[ext]).isEmpty()
    }
}
