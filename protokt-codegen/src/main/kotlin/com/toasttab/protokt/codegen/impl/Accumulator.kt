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

import com.squareup.kotlinpoet.FileSpec
import com.toasttab.protokt.codegen.model.Import
import com.toasttab.protokt.codegen.protoc.Protocol

internal object Accumulator {
    fun buildFile(
        protocol: Protocol,
        imports: Set<Import>,
        fileDescriptorInfo: FileDescriptorInfo?
    ): FileSpec? {
        val descs = Annotator.apply(protocol)
        if (descs.isEmpty() && protocol.desc.context.lite) {
            return null
        }

        val accumulatedImports =
            fileDescriptorInfo?.let {
                imports + it.imports.map(Import::Literal)
            } ?: imports

        val builder = HeaderAccumulator.startFile(protocol, accumulatedImports)

        descs.forEach {
            builder.addType(it.type.typeSpec)
        }

        if (fileDescriptorInfo != null) {
            builder.addType(fileDescriptorInfo.fdp)
            fileDescriptorInfo.properties.forEach(builder::addProperty)
        }

        return builder.build()
    }
}
