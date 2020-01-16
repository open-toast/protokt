/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.codegen.impl

import com.google.protobuf.DescriptorProtos.DescriptorProto.ENUM_TYPE_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto.VALUE_FIELD_NUMBER
import com.toasttab.protokt.codegen.EnumType
import com.toasttab.protokt.codegen.impl.MessageDocumentationAnnotator.baseLocation
import com.toasttab.protokt.codegen.impl.STAnnotator.Context

internal class EnumDocumentationAnnotator
private constructor(
    enum: EnumType,
    private val ctx: Context
) {
    private val enumPath = listOf(ENUM_TYPE_FIELD_NUMBER, enum.index)

    private fun annotateEnumDocumentation() =
        baseLocation(ctx, enumPath)
            .cleanDocumentation()

    private fun annotateEnumFieldDocumentation(value: EnumType.Value) =
        baseLocation(ctx, enumPath + listOf(VALUE_FIELD_NUMBER, value.index))
            .cleanDocumentation()

    companion object {
        fun annotateEnumDocumentation(enum: EnumType, ctx: Context) =
            EnumDocumentationAnnotator(enum, ctx).annotateEnumDocumentation()

        fun annotateEnumFieldDocumentation(
            enum: EnumType,
            value: EnumType.Value,
            ctx: Context
        ) =
            EnumDocumentationAnnotator(enum, ctx)
                .annotateEnumFieldDocumentation(value)
    }
}
