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

package protokt.v1.codegen.generate

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import protokt.v1.Extension
import protokt.v1.ExtensionCodecs
import protokt.v1.RepeatedExtension
import protokt.v1.codegen.util.ProtoExtension
import protokt.v1.codegen.util.ProtoFileContents
import protokt.v1.reflect.FieldType

internal fun generateExtensions(contents: ProtoFileContents): List<PropertySpec> {
    if (!contents.info.context.generateTypes || !contents.info.context.kotlinTarget.isPrimaryTarget) {
        return emptyList()
    }
    return contents.extensions.flatMap { ext ->
        listOf(generateExtensionProperty(ext), generateExtensionAccessor(ext))
    }
}

private fun generateExtensionProperty(ext: ProtoExtension): PropertySpec {
    val containerClass =
        if (ext.repeated) {
            RepeatedExtension::class.asTypeName()
        } else {
            Extension::class.asTypeName()
        }

    val valueTypeName = ext.valueClassName ?: ext.fieldType.protoktFieldType.asTypeName()
    val propertyType = containerClass.parameterizedBy(ext.extendee, valueTypeName)

    return PropertySpec
        .builder("${ext.fieldName}Extension", propertyType)
        .addModifiers(com.squareup.kotlinpoet.KModifier.PRIVATE)
        .initializer(
            "%T(%Lu, %L)",
            if (ext.repeated) RepeatedExtension::class else Extension::class,
            ext.number,
            codecFor(ext)
        )
        .build()
}

private fun generateExtensionAccessor(ext: ProtoExtension): PropertySpec {
    val valueTypeName = ext.valueClassName ?: ext.fieldType.protoktFieldType.asTypeName()

    val returnType =
        if (ext.repeated) {
            List::class.asTypeName().parameterizedBy(valueTypeName)
        } else {
            valueTypeName.copy(nullable = true)
        }

    return PropertySpec
        .builder(ext.fieldName, returnType)
        .addAnnotation(
            com.squareup.kotlinpoet.AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "EXTENSION_SHADOWED_BY_MEMBER")
                .build()
        )
        .receiver(ext.extendee)
        .getter(
            com.squareup.kotlinpoet.FunSpec.getterBuilder()
                .addCode("return this[${ext.fieldName}Extension]")
                .build()
        )
        .build()
}

private fun codecFor(ext: ProtoExtension): CodeBlock =
    when (ext.fieldType) {
        FieldType.Message ->
            CodeBlock.of("%T.message(%T)", ExtensionCodecs::class, ext.valueClassName!!)

        FieldType.Enum ->
            CodeBlock.of("%T.enum(%T)", ExtensionCodecs::class, ext.valueClassName!!)

        FieldType.Bool -> CodeBlock.of("%T.bool", ExtensionCodecs::class)

        FieldType.Double -> CodeBlock.of("%T.double", ExtensionCodecs::class)

        FieldType.Float -> CodeBlock.of("%T.float", ExtensionCodecs::class)

        FieldType.Fixed32 -> CodeBlock.of("%T.fixed32", ExtensionCodecs::class)

        FieldType.Fixed64 -> CodeBlock.of("%T.fixed64", ExtensionCodecs::class)

        FieldType.Int32 -> CodeBlock.of("%T.int32", ExtensionCodecs::class)

        FieldType.Int64 -> CodeBlock.of("%T.int64", ExtensionCodecs::class)

        FieldType.SFixed32 -> CodeBlock.of("%T.sfixed32", ExtensionCodecs::class)

        FieldType.SFixed64 -> CodeBlock.of("%T.sfixed64", ExtensionCodecs::class)

        FieldType.SInt32 -> CodeBlock.of("%T.sint32", ExtensionCodecs::class)

        FieldType.SInt64 -> CodeBlock.of("%T.sint64", ExtensionCodecs::class)

        FieldType.UInt32 -> CodeBlock.of("%T.uint32", ExtensionCodecs::class)

        FieldType.UInt64 -> CodeBlock.of("%T.uint64", ExtensionCodecs::class)

        FieldType.String -> CodeBlock.of("%T.string", ExtensionCodecs::class)

        FieldType.Bytes -> CodeBlock.of("%T.bytes", ExtensionCodecs::class)
    }
