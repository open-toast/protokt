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

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.Descriptors.EnumValueDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.DynamicMessage
import com.google.protobuf.InvalidProtocolBufferException
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.EnumDeserializer
import protokt.v1.ExtensionCodec
import protokt.v1.ExtensionCodecs
import protokt.v1.GeneratedProperty
import protokt.v1.Message
import protokt.v1.ProtoktDecodeException
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

    private fun getUnknownField(field: FieldDescriptor, message: Message): Any? {
        val unknown = message.unknownFields[field.number.toUInt()]
            ?: return if (field.isMapField) {
                emptyMap<Any, Any>()
            } else if (field.isRepeated) {
                emptyList<Any>()
            } else {
                null
            }

        if (field.type == Type.MESSAGE) {
            val values = unknown.lengthDelimited.map { decodeMessage(field, it.value) }
            return when {
                field.isMapField -> {
                    val key = field.messageType.findFieldByNumber(1)
                    val value = field.messageType.findFieldByNumber(2)
                    values.associate { entry ->
                        mapValue(key, entry.getField(key)) to mapValue(value, entry.getField(value))
                    }
                }

                field.isRepeated -> values

                else -> values.lastOrNull()
            }
        }

        val codec = scalarCodec(field) ?: return if (field.isRepeated) emptyList<Any>() else null
        return if (field.isRepeated) codec.decodeRepeated(unknown) else codec.decodeSingular(unknown)
    }

    private fun scalarCodec(field: FieldDescriptor): ExtensionCodec<*>? =
        when (field.type) {
            Type.INT32 -> ExtensionCodecs.int32
            Type.INT64 -> ExtensionCodecs.int64
            Type.UINT32 -> ExtensionCodecs.uint32
            Type.UINT64 -> ExtensionCodecs.uint64
            Type.SINT32 -> ExtensionCodecs.sint32
            Type.SINT64 -> ExtensionCodecs.sint64
            Type.BOOL -> ExtensionCodecs.bool
            Type.ENUM -> ExtensionCodecs.enum(ReflectedUnknownEnum)
            Type.FIXED32 -> ExtensionCodecs.fixed32
            Type.SFIXED32 -> ExtensionCodecs.sfixed32
            Type.FLOAT -> ExtensionCodecs.float
            Type.FIXED64 -> ExtensionCodecs.fixed64
            Type.SFIXED64 -> ExtensionCodecs.sfixed64
            Type.DOUBLE -> ExtensionCodecs.double
            Type.STRING -> ExtensionCodecs.string
            Type.BYTES -> ExtensionCodecs.bytes
            else -> null
        }

    private fun decodeMessage(field: FieldDescriptor, bytes: Bytes): DynamicMessage =
        try {
            DynamicMessage.parseFrom(field.messageType, CodedInputStream.newInstance(bytes.asReadOnlyBuffer()))
        } catch (e: InvalidProtocolBufferException) {
            throw ProtoktDecodeException(e.message ?: "Malformed protobuf input", e)
        }

    private fun mapValue(field: FieldDescriptor, value: Any): Any =
        when (field.type) {
            Type.UINT32, Type.FIXED32 -> (value as Int).toUInt()
            Type.UINT64, Type.FIXED64 -> (value as Long).toULong()
            Type.ENUM -> ReflectedUnknownEnum((value as EnumValueDescriptor).number)
            Type.BYTES -> Bytes.from((value as ByteString).toByteArray())
            else -> value
        }

    fun getField(message: Message, field: FieldDescriptor): Any? =
        getReflectedGettersByClass(message::class)(field, message)
}

private class ReflectedUnknownEnum(
    override val value: Int
) : Enum() {
    override val name = "UNRECOGNIZED"

    companion object : EnumDeserializer<ReflectedUnknownEnum> {
        override fun deserialize(value: Int) =
            ReflectedUnknownEnum(value)
    }
}
