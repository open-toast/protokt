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

package protokt.v1.google.protobuf

import com.google.protobuf.CodedInputStream
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.DynamicMessage
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Fixed32Val
import protokt.v1.Fixed64Val
import protokt.v1.GeneratedProperty
import protokt.v1.LengthDelimitedVal
import protokt.v1.Message
import protokt.v1.UnknownValue
import protokt.v1.VarintVal
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import kotlin.Any
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

internal object ProtoktReflect {
    private val reflectedGettersByClass = ConcurrentHashMap<KClass<out Message>, (FieldDescriptor, Message) -> Any?>()

    private fun getReflectedGettersByClass(messageClass: KClass<out Message>) =
        reflectedGettersByClass.computeIfAbsent(messageClass) {
            { field, message ->
                topLevelProperty(messageClass)(field, message)
                    ?: oneofProperty(messageClass)(field, message)
                    ?: getUnknownField(field, message)
            }
        }

    private fun topLevelProperty(klass: KClass<out Message>): (FieldDescriptor, Message) -> Any? {
        val gettersByNumber = gettersByNumber<Message>(klass)
        return { field, instance -> gettersByNumber[field.number]?.invoke(instance) }
    }

    private fun <T> gettersByNumber(klass: KClass<*>): Map<Int, KProperty1<T, Any?>> =
        klass.declaredMemberProperties
            .map { it.findAnnotation<GeneratedProperty>()?.number to it }
            .filter { (number, _) -> number != null }
            .associate { (number, getter) ->
                @Suppress("UNCHECKED_CAST")
                number!! to getter as KProperty1<T, Any?>
            }

    private fun oneofProperty(messageClass: KClass<out Message>): (FieldDescriptor, Message) -> Any? {
        val oneofPropertiesSealedClasses =
            messageClass
                .nestedClasses
                .filter { it.isSealed && !it.isSubclassOf(Enum::class) }

        val gettersByNumber =
            buildMap {
                oneofPropertiesSealedClasses.forEach { sealedClass ->
                    val oneofPropertyGetter =
                        messageClass.declaredMemberProperties
                            .single {
                                it.returnType.classifier == sealedClass &&
                                    // prevent selection of non-null accessor
                                    it.returnType.isMarkedNullable
                            }
                            .let {
                                @Suppress("UNCHECKED_CAST")
                                it as KProperty1<Message, *>
                            }

                    sealedClass.nestedClasses.forEach { sealedClassSubtype ->
                        val (number, getterFromSubtype) = gettersByNumber<Any>(sealedClassSubtype).entries.single()
                        put(number) { msg: Message ->
                            val oneofProperty = oneofPropertyGetter.get(msg)
                            if (sealedClassSubtype.isInstance(oneofProperty)) {
                                getterFromSubtype(oneofProperty!!)
                            } else {
                                null
                            }
                        }
                    }
                }
            }

        return { field, msg -> gettersByNumber[field.number]?.invoke(msg) }
    }

    private fun getUnknownField(field: FieldDescriptor, message: Message) =
        message.unknownFields[field.number.toUInt()]?.values?.flatMap { value ->
            decodeUnknownValue(field, value)
        }.let {
            if (field.isRepeated) {
                if (field.isMapField) {
                    it.orEmpty()
                        .filterIsInstance<DynamicMessage>()
                        .associate { entry ->
                            entry.getField(field.messageType.findFieldByNumber(1)) to
                                entry.getField(field.messageType.findFieldByNumber(2))
                        }
                } else {
                    it.orEmpty()
                }
            } else {
                it?.lastOrNull()
            }
        }

    private fun decodeUnknownValue(field: FieldDescriptor, value: UnknownValue): List<Any> =
        when (value) {
            is VarintVal -> decodeVarint(field, value.value)?.let(::listOf).orEmpty()
            is Fixed32Val -> decodeFixed32(field, value.value)?.let(::listOf).orEmpty()
            is Fixed64Val -> decodeFixed64(field, value.value)?.let(::listOf).orEmpty()
            is LengthDelimitedVal -> decodeLengthDelimited(field, value.value)
        }

    private fun decodeVarint(field: FieldDescriptor, value: ULong): Any? =
        when (field.type) {
            Type.INT32 -> value.toInt()
            Type.INT64 -> value.toLong()
            Type.UINT32 -> value.toUInt()
            Type.UINT64 -> value
            Type.SINT32 -> value.toInt().let { (it ushr 1) xor -(it and 1) }
            Type.SINT64 -> value.toLong().let { (it ushr 1) xor -(it and 1) }
            Type.BOOL -> value != 0uL
            Type.ENUM -> ReflectedUnknownEnum(value.toInt())
            else -> null
        }

    private fun decodeFixed32(field: FieldDescriptor, value: UInt): Any? =
        when (field.type) {
            Type.FIXED32 -> value
            Type.SFIXED32 -> value.toInt()
            Type.FLOAT -> Float.fromBits(value.toInt())
            else -> null
        }

    private fun decodeFixed64(field: FieldDescriptor, value: ULong): Any? =
        when (field.type) {
            Type.FIXED64 -> value
            Type.SFIXED64 -> value.toLong()
            Type.DOUBLE -> Double.fromBits(value.toLong())
            else -> null
        }

    private fun decodeLengthDelimited(field: FieldDescriptor, value: Bytes): List<Any> =
        when (field.type) {
            Type.STRING -> listOf(StandardCharsets.UTF_8.decode(value.asReadOnlyBuffer()).toString())

            Type.BYTES -> listOf(value)

            Type.MESSAGE ->
                listOf(
                    DynamicMessage.parseFrom(
                        field.messageType,
                        CodedInputStream.newInstance(value.asReadOnlyBuffer()),
                    )
                )

            else -> if (field.isRepeated && field.isPackable) decodePacked(field, value) else emptyList()
        }

    private fun decodePacked(field: FieldDescriptor, value: Bytes): List<Any> {
        val input = CodedInputStream.newInstance(value.asReadOnlyBuffer())
        return buildList {
            while (!input.isAtEnd) {
                add(
                    when (field.type) {
                        Type.INT32 -> input.readInt32()
                        Type.INT64 -> input.readInt64()
                        Type.UINT32 -> input.readUInt32().toUInt()
                        Type.UINT64 -> input.readUInt64().toULong()
                        Type.SINT32 -> input.readSInt32()
                        Type.SINT64 -> input.readSInt64()
                        Type.FIXED32 -> input.readFixed32().toUInt()
                        Type.FIXED64 -> input.readFixed64().toULong()
                        Type.SFIXED32 -> input.readSFixed32()
                        Type.SFIXED64 -> input.readSFixed64()
                        Type.FLOAT -> input.readFloat()
                        Type.DOUBLE -> input.readDouble()
                        Type.BOOL -> input.readBool()
                        Type.ENUM -> ReflectedUnknownEnum(input.readEnum())
                        else -> error("field ${field.fullName} is not packable")
                    }
                )
            }
        }
    }

    fun getField(message: Message, field: FieldDescriptor): Any? =
        getReflectedGettersByClass(message::class)(field, message)
}

private class ReflectedUnknownEnum(
    override val value: Int
) : Enum() {
    override val name = "UNRECOGNIZED"
}
