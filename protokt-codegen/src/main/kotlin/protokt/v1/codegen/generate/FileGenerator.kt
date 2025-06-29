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

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import protokt.v1.codegen.util.ProtoFileContents
import protokt.v1.gradle.KotlinTarget
import protokt.v1.reflect.PROTOKT_V1

internal fun generateFile(contents: ProtoFileContents) =
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
                addAnnotation(
                    AnnotationSpec.builder(Suppress::class).apply {
                        addMember("DEPRECATION".embed())
                        // Suppresses a failure due to usage of @JvmStatic in common code. It seems prudent to add this
                        // for all targets but it fails on the Android target.
                        //
                        // See https://youtrack.jetbrains.com/issue/KTIJ-22326
                        if (contents.info.context.kotlinTarget == KotlinTarget.MultiplatformCommon) {
                            addMember("OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE".embed())
                        }
                    }.build()
                )

                addAnnotation(
                    AnnotationSpec.builder(ClassName.bestGuess("kotlin.OptIn"))
                        .addMember("$PROTOKT_V1.OnlyForUseByGeneratedProtoCode::class")
                        .build()
                )

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

        if (contents.info.context.generateDescriptors && contents.info.context.kotlinTarget.isPrimaryTarget) {
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
