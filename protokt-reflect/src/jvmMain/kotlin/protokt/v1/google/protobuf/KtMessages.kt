/*
 * Copyright (c) 2023 Toast, Inc.
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

@file:JvmName("KtMessages")

package protokt.v1.google.protobuf

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.MapEntry
import com.google.protobuf.Message
import com.google.protobuf.UnknownFieldSet
import com.google.protobuf.UnknownFieldSet.Field
import com.google.protobuf.UnsafeByteOperations
import com.google.protobuf.WireFormat
import com.toasttab.protokt.v1.ProtoktProtos
import io.github.classgraph.ClassGraph
import protokt.v1.Bytes
import protokt.v1.Converter
import protokt.v1.KtEnum
import protokt.v1.KtGeneratedFileDescriptor
import protokt.v1.KtGeneratedMessage
import protokt.v1.KtMessage
import protokt.v1.reflect.ClassLookup
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.inferClassName
import protokt.v1.reflect.resolvePackage
import protokt.v1.reflect.typeName
import kotlin.Any
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

fun KtMessage.toDynamicMessage(context: RuntimeContext): DynamicMessage =
    context.convertValue(this) as DynamicMessage

class RuntimeContext internal constructor(
    descriptors: Iterable<Descriptors.Descriptor>,
    private val classLookup: ClassLookup
) {
    internal val descriptorsByTypeName = descriptors.associateBy { it.fullName }

    fun convertValue(value: Any?) =
        when (value) {
            is KtEnum -> value.value
            is UInt -> value.toInt()
            is ULong -> value.toLong()
            is KtMessage -> toDynamicMessage(value, this)
            is Bytes -> UnsafeByteOperations.unsafeWrap(value.asReadOnlyBuffer())

            // pray
            else -> value
        }

    internal fun unwrap(value: Any, field: FieldDescriptor, wrap: String): Any {
        val proto = field.toProto()
        val type = FieldType.from(proto.type)
        val converterDetails =
            classLookup.converter(
                ClassLookup.evaluateProtobufTypeCanonicalName(
                    proto.typeName,
                    typeName(proto.typeName, type),
                    type,
                    field.name
                ),
                inferClassName(wrap, resolvePackage(field.file.`package`))
                    .let { (pkg, names) -> pkg + "." + names.joinToString(".") }
            )

        @Suppress("UNCHECKED_CAST")
        val converter = converterDetails.converter as Converter<Any, Any>
        return converter.unwrap(value)
    }

    companion object {
        private val reflectiveContext by lazy {
            RuntimeContext(getDescriptors(), ClassLookup(emptyList()))
        }

        fun getContextReflectively() =
            reflectiveContext
    }
}

private fun getDescriptors() =
    ClassGraph()
        .enableAnnotationInfo()
        .scan()
        .use {
            it.getClassesWithAnnotation(KtGeneratedFileDescriptor::class.java)
                .map { info ->
                    @Suppress("UNCHECKED_CAST")
                    info.loadClass().kotlin as KClass<Any>
                }
        }
        .asSequence()
        .map { klassWithDescriptor ->
            klassWithDescriptor
                .declaredMemberProperties
                .single { it.returnType.classifier == FileDescriptor::class }
                .get(klassWithDescriptor.objectInstance!!) as FileDescriptor
        }
        .flatMap { it.toProtobufJavaDescriptor().messageTypes }
        .flatMap(::collectDescriptors)
        .asIterable()

private fun collectDescriptors(descriptor: Descriptors.Descriptor): Iterable<Descriptors.Descriptor> =
    listOf(descriptor) + descriptor.nestedTypes.flatMap(::collectDescriptors)

private fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
    Descriptors.FileDescriptor.buildFrom(
        DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
        dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
        true,
    )

private fun toDynamicMessage(
    message: KtMessage,
    context: RuntimeContext,
): Message {
    val descriptor =
        context.descriptorsByTypeName
            .getValue(message::class.findAnnotation<KtGeneratedMessage>()!!.fullTypeName)

    return DynamicMessage.newBuilder(descriptor)
        .apply {
            descriptor.fields.forEach { field ->
                ProtoktReflect.getField(message, field)?.let { value ->
                    val wrap = wrap(field)

                    setField(
                        field,
                        when {
                            field.type == Descriptors.FieldDescriptor.Type.ENUM ->
                                if (field.isRepeated) {
                                    (value as List<*>).map { field.enumType.findValueByNumberCreatingIfUnknown(((it as KtEnum).value)) }
                                } else {
                                    field.enumType.findValueByNumberCreatingIfUnknown(((value as KtEnum).value))
                                }

                            // todo: unwrap keys and values if wrapped
                            field.isMapField ->
                                convertMap(value, field, context)

                            // todo: unwrap elements if wrapped
                            field.isRepeated ->
                                (value as List<*>).map {
                                    if (isWrapped(field, wrap)) {
                                        context.convertValue(context.unwrap(it!!, field, wrap!!))
                                    } else {
                                        context.convertValue(it)
                                    }
                                }

                            isWrapped(field, wrap) ->
                                context.convertValue(context.unwrap(value, field, wrap!!))

                            else -> context.convertValue(value)
                        },
                    )
                }
            }
        }
        .setUnknownFields(mapUnknownFields(message))
        .build()
}

private val WRAPPER_TYPE_NAMES =
    setOf(
        "google.protobuf.DoubleValue",
        "google.protobuf.FloatValue",
        "google.protobuf.Int64Value",
        "google.protobuf.UInt64Value",
        "google.protobuf.Int32Value",
        "google.protobuf.UInt32Value",
        "google.protobuf.BoolValue",
        "google.protobuf.StringValue",
        "google.protobuf.BytesValue"
    )

private fun isWrapped(field: FieldDescriptor, wrap: String?): Boolean {
    if (field.type == FieldDescriptor.Type.MESSAGE && field.messageType.fullName in WRAPPER_TYPE_NAMES) {
        return true
    }

    return wrap != null
}

private fun wrap(field: FieldDescriptor): String? {
    if (field.type == FieldDescriptor.Type.MESSAGE && field.messageType.fullName in WRAPPER_TYPE_NAMES) {
        return field.messageType.fullName
    }

    val options = field.toProto().options

    val propertyOptions =
        if (options.hasField(ProtoktProtos.property.descriptor)) {
            options.getField(ProtoktProtos.property.descriptor) as ProtoktProtos.FieldOptions
        } else if (options.unknownFields.hasField(ProtoktProtos.property.number)) {
            ProtoktProtos.FieldOptions.parseFrom(
                options.unknownFields.getField(ProtoktProtos.property.number)
                    .lengthDelimitedList
                    .last()
            )
        } else {
            return null
        }

    return propertyOptions.wrap.takeIf { it.isNotEmpty() }
        ?: propertyOptions.keyWrap.takeIf { it.isNotEmpty() }
        ?: propertyOptions.valueWrap.takeIf { it.isNotEmpty() }
}

private fun convertMap(
    value: Any,
    field: FieldDescriptor,
    context: RuntimeContext,
): List<MapEntry<*, *>> {
    val keyDesc = field.messageType.findFieldByNumber(1)
    val valDesc = field.messageType.findFieldByNumber(2)

    val keyDefault =
        if (keyDesc.type == Descriptors.FieldDescriptor.Type.MESSAGE) {
            null
        } else {
            keyDesc.defaultValue
        }

    val valDefault =
        if (valDesc.type == Descriptors.FieldDescriptor.Type.MESSAGE) {
            null
        } else {
            valDesc.defaultValue
        }

    val defaultEntry =
        MapEntry.newDefaultInstance(
            field.messageType,
            WireFormat.FieldType.valueOf(keyDesc.type.name),
            keyDefault,
            WireFormat.FieldType.valueOf(valDesc.type.name),
            valDefault
        ) as MapEntry<Any?, Any?>

    return (value as Map<*, *>).map { (k, v) ->
        defaultEntry.toBuilder()
            .setKey(context.convertValue(k))
            .setValue(context.convertValue(v))
            .build()
    }
}

private fun mapUnknownFields(message: KtMessage): UnknownFieldSet {
    val unknownFields = UnknownFieldSet.newBuilder()

    getUnknownFields(message).forEach { (number, field) ->
        unknownFields.mergeField(
            number.toInt(),
            Field.newBuilder()
                .apply {
                    field.varint.forEach { addVarint(it.value.toLong()) }
                    field.fixed32.forEach { addFixed32(it.value.toInt()) }
                    field.fixed64.forEach { addFixed64(it.value.toLong()) }
                    field.lengthDelimited.forEach { addLengthDelimited(UnsafeByteOperations.unsafeWrap(it.value.asReadOnlyBuffer())) }
                }
                .build()
        )
    }

    return unknownFields.build()
}
