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

import com.squareup.kotlinpoet.FileSpec
import protokt.v1.codegen.generate.Deprecation.addDeprecationSuppression
import protokt.v1.codegen.util.Message
import protokt.v1.codegen.util.ProtoFileContents

fun generateFile(contents: ProtoFileContents) =
    FileGenerator(contents).generate()

private class FileGenerator(
    private val contents: ProtoFileContents
) {
    fun generate(): FileSpec? {
        val descs = CodeGenerator.generate(contents)

        val builder =
            FileSpec.builder(
                contents.info.kotlinPackage,
                fileName(contents)
            ).apply {
                addDeprecationSuppression()
                // https://github.com/square/kotlinpoet/pull/533
                addFileComment(
                    """
                        Generated by protokt version ${contents.info.context.protoktVersion}. Do not modify.
                        Source: ${contents.info.name}
                    """.trimIndent()
                )
                indent(INDENT)
            }

        var anyCodeAdded = false

        descs.forEach {
            anyCodeAdded = true
            builder.addType(it.typeSpec)
        }

        if (contents.info.context.generateDescriptors) {
            val fileDescriptorInfo = FileDescriptorResolver.resolveFileDescriptor(contents)

            if (fileDescriptorInfo != null) {
                anyCodeAdded = true
                builder.addType(fileDescriptorInfo.fdp)
                fileDescriptorInfo.properties.forEach(builder::addProperty)
            }
        }

        return if (anyCodeAdded) builder.build() else null
    }
}

private fun fileName(contents: ProtoFileContents): String {
    val pkg = contents.info.kotlinPackage
    val name = contents.info.name
    val dir = pkg.replace('.', '/') + '/'
    val fileNameBase = name.substringAfterLast('/').removeSuffix(".proto")

    return dir + fileNameBase + suffixes(contents).joinToString("") + ".kt"
}

private fun suffixes(contents: ProtoFileContents): List<String> {
    val suffixes = mutableListOf<String>()
    if (!contents.info.context.generateTypes) {
        if (contents.info.context.generateDescriptors) {
            suffixes.add("_descriptors")
        }
        if (contents.info.context.generateGrpcDescriptors) {
            suffixes.add("_grpc")
        }
        if (contents.info.context.generateGrpcKotlinStubs) {
            suffixes.add("_grpc_kotlin")
        }
    }
    return suffixes
}
