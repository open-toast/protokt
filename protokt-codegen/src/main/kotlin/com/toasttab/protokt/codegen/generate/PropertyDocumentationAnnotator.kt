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

package com.toasttab.protokt.codegen.generate

import com.google.protobuf.DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER
import com.google.protobuf.DescriptorProtos.DescriptorProto.ONEOF_DECL_FIELD_NUMBER
import com.toasttab.protokt.codegen.generate.CodeGenerator.Context
import com.toasttab.protokt.codegen.util.Field
import com.toasttab.protokt.codegen.util.Oneof
import com.toasttab.protokt.codegen.util.StandardField

fun annotatePropertyDocumentation(field: Field, ctx: Context) =
    PropertyDocumentationAnnotator(field, ctx).annotate()

private class PropertyDocumentationAnnotator(
    private val field: Field,
    private val ctx: Context
) {
    fun annotate() =
        baseLocation(
            ctx,
            when (field) {
                is StandardField -> listOf(FIELD_FIELD_NUMBER, field.index)
                is Oneof -> listOf(ONEOF_DECL_FIELD_NUMBER, field.index)
            }
        ).cleanDocumentation()
}
