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

@file:JvmName("Messages")

package protokt.v1.google.protobuf

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.DynamicMessage
import com.google.protobuf.MapEntry
import com.google.protobuf.UnknownFieldSet
import com.google.protobuf.UnknownFieldSet.Field
import com.google.protobuf.UnsafeByteOperations
import com.google.protobuf.WireFormat
import com.toasttab.protokt.v1.ProtoktProtos
import io.github.classgraph.ClassGraph
import protokt.v1.Bytes
import protokt.v1.Converter
import protokt.v1.Enum
import protokt.v1.GeneratedFileDescriptor
import protokt.v1.GeneratedMessage
import protokt.v1.Message
import protokt.v1.reflect.ClassLookup
import protokt.v1.reflect.FieldType
import protokt.v1.reflect.WellKnownTypes
import protokt.v1.reflect.inferClassName
import protokt.v1.reflect.resolvePackage
import protokt.v1.reflect.typeName
import kotlin.Any
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import com.google.protobuf.Message as ProtobufMessage

fun Message.toDynamicMessage(context: RuntimeContext): DynamicMessage =
    context.convertValue(this) as DynamicMessage

fun Message.hasField(field: FieldDescriptor): Boolean {
    val value = getField(field)

    return if (field.hasPresence()) {
        value != null
    } else {
        value != defaultValue(field)
    }
}

private fun defaultValue(field: FieldDescriptor) =
    when (field.type) {
        Type.UINT64, Type.FIXED64 -> 0uL
        Type.UINT32, Type.FIXED32 -> 0u
        Type.BYTES -> Bytes.empty()
        else -> field.defaultValue
    }

fun Message.getField(field: FieldDescriptor) =
    ProtoktReflect.getField(this, field)

class RuntimeContext internal constructor(
    descriptors: Iterable<Descriptors.Descriptor>,
    private val classLookup: ClassLookup
) {
    constructor(descriptors: Iterable<Descriptors.Descriptor>) : this(descriptors, DEFAULT_CLASS_LOOKUP)

    internal val descriptorsByTypeName = descriptors.associateBy { it.fullName }

    fun convertValue(value: Any) =
        when (value) {
            is Enum -> value.value
            is UInt -> value.toInt()
            is ULong -> value.toLong()
            is Message -> toDynamicMessage(value, this)
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
                wrap
            )

        @Suppress("UNCHECKED_CAST")
        val converter = converterDetails.converter as Converter<Any, Any>
        return converter.unwrap(value)
    }

    companion object {
        private val DEFAULT_CLASS_LOOKUP by lazy { ClassLookup(emptyList()) }

        private val reflectiveContext by lazy {
            RuntimeContext(getDescriptors(), DEFAULT_CLASS_LOOKUP)
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
            it.getClassesWithAnnotation(GeneratedFileDescriptor::class.java)
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
    message: Message,
    context: RuntimeContext,
): ProtobufMessage {
    val descriptor =
        context.descriptorsByTypeName
            .getValue(message::class.findAnnotation<GeneratedMessage>()!!.fullTypeName)

    return DynamicMessage.newBuilder(descriptor)
        .apply {
            descriptor.fields.forEach { field ->
                message.getField(field)?.let { value ->
                    val fieldOptions = fieldOptions(field)

                    setField(
                        field,
                        when {
                            field.type == Type.ENUM ->
                                if (field.isRepeated) {
                                    (value as List<*>).map { field.enumType.findValueByNumberCreatingIfUnknown(((it as Enum).value)) }
                                } else {
                                    field.enumType.findValueByNumberCreatingIfUnknown(((value as Enum).value))
                                }

                            field.isMapField ->
                                convertMap(value, field, fieldOptions, context)

                            field.isRepeated ->
                                convertList((value as List<*>), field, fieldOptions, context)

                            else -> {
                                val wrap = wrap(field, fieldOptions)
                                if (wrap == null) {
                                    context.convertValue(value)
                                } else {
                                    context.convertValue(context.unwrap(value, field, wrap))
                                }
                            }
                        },
                    )
                }
            }
        }
        .setUnknownFields(mapUnknownFields(message))
        .build()
}

private fun fieldOptions(field: FieldDescriptor): ProtoktProtos.FieldOptions {
    val options = field.toProto().options

    return if (options.hasField(ProtoktProtos.property.descriptor)) {
        options.getField(ProtoktProtos.property.descriptor) as ProtoktProtos.FieldOptions
    } else if (options.unknownFields.hasField(ProtoktProtos.property.number)) {
        ProtoktProtos.FieldOptions.parseFrom(
            options.unknownFields.getField(ProtoktProtos.property.number)
                .lengthDelimitedList
                .last()
        )
    } else {
        ProtoktProtos.FieldOptions.getDefaultInstance()
    }
}

private fun wrap(field: FieldDescriptor, fieldOptions: ProtoktProtos.FieldOptions) =
    getClassName(fieldOptions.wrap, field)

private fun getClassName(wrap: String, field: FieldDescriptor): String? =
    WellKnownTypes.wrapWithWellKnownInterception(wrap.takeIf { it.isNotEmpty() }, field.toProto().typeName)
        ?.let { inferClassName(it, resolvePackage(field.file.`package`)) }
        ?.let { (pkg, names) -> pkg + "." + names.joinToString(".") }

private fun convertList(
    value: List<*>,
    field: FieldDescriptor,
    fieldOptions: ProtoktProtos.FieldOptions,
    context: RuntimeContext
): List<*> {
    val wrap = wrap(field, fieldOptions)
    return value.map {
        if (wrap == null) {
            it?.let(context::convertValue)
        } else {
            context.convertValue(context.unwrap(it!!, field, wrap))
        }
    }
}

private fun convertMap(
    value: Any,
    field: FieldDescriptor,
    fieldOptions: ProtoktProtos.FieldOptions,
    context: RuntimeContext
): List<MapEntry<*, *>> {
    val keyDesc = field.messageType.findFieldByNumber(1)
    val valDesc = field.messageType.findFieldByNumber(2)

    val keyDefault =
        if (keyDesc.type == Type.MESSAGE) {
            null
        } else {
            keyDesc.defaultValue
        }

    val valDefault =
        if (valDesc.type == Type.MESSAGE) {
            null
        } else {
            valDesc.defaultValue
        }

    val keyWrap = getClassName(fieldOptions.keyWrap, keyDesc)
    val valWrap = getClassName(fieldOptions.valueWrap, valDesc)

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
            .setKey(
                if (keyWrap == null) {
                    k?.let(context::convertValue)
                } else {
                    context.convertValue(context.unwrap(k!!, keyDesc, keyWrap))
                }
            )
            .setValue(
                if (valWrap == null) {
                    v?.let(context::convertValue)
                } else {
                    context.convertValue(context.unwrap(v!!, valDesc, valWrap))
                }
            )
            .build()
    }
}

private fun mapUnknownFields(message: Message): UnknownFieldSet {
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
