/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.TypeSpec
import protokt.v1.codegen.util.Enum
import protokt.v1.codegen.util.ErrorContext.withEnumName
import protokt.v1.codegen.util.ErrorContext.withMessageName
import protokt.v1.codegen.util.ErrorContext.withServiceName
import protokt.v1.codegen.util.GeneratedType
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.ProtoFileContents
import protokt.v1.codegen.util.ProtoFileInfo
import protokt.v1.codegen.util.Service
import protokt.v1.codegen.util.TopLevelType

internal object CodeGenerator {
    data class Context(
        val enclosing: List<Message>,
        val info: ProtoFileInfo
    )

    fun generate(contents: ProtoFileContents) =
        contents.types.flatMap {
            generate(it, Context(emptyList(), contents.info)).map(::GeneratedType)
        }

    fun generate(type: TopLevelType, ctx: Context): Iterable<TypeSpec> =
        when (type) {
            is Message ->
                withMessageName(type.className) {
                    listOfNotNull(
                        generateMessage(
                            type,
                            ctx.copy(enclosing = ctx.enclosing + type)
                        )
                    )
                }
            is Enum ->
                withEnumName(type.className) {
                    listOfNotNull(generateEnum(type, ctx))
                }
            is Service ->
                withServiceName(type.name) {
                    generateService(
                        type,
                        ctx,
                        ctx.info.context.appliedKotlinPlugin
                    )
                }
        }
}
