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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import protokt.v1.Bytes
import protokt.v1.Fixed32Val
import protokt.v1.Fixed64Val
import protokt.v1.KtEnum
import protokt.v1.KtMessage
import protokt.v1.KtProperty
import protokt.v1.LengthDelimitedVal
import protokt.v1.UnknownFieldSet
import protokt.v1.VarintVal
import java.nio.charset.StandardCharsets
import kotlin.Any
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf

object ProtoktReflect {
    private val reflectedGettersByClass =
        CacheBuilder.newBuilder()
            .build(
                object : CacheLoader<KClass<out KtMessage>, (FieldDescriptor, KtMessage) -> Any?>() {
                    override fun load(messageClass: KClass<out KtMessage>) =
                        { field: FieldDescriptor, message: KtMessage ->
                            topLevelProperty(messageClass)(field, message)
                                ?: oneofProperty(messageClass)(field, message)
                                ?: getUnknownField(field, message)
                        }
                },
            )

    private fun topLevelProperty(klass: KClass<out KtMessage>): (FieldDescriptor, KtMessage) -> Any? {
        val gettersByNumber = gettersByNumber<KtMessage>(klass)
        return { field, instance -> gettersByNumber[field.number]?.invoke(instance) }
    }

    private fun <T> gettersByNumber(klass: KClass<*>): Map<Int, KProperty1<T, Any?>> =
        klass.declaredMemberProperties
            .map { it.findAnnotation<KtProperty>()?.number to it }
            .filter { (number, _) -> number != null }
            .associate { (number, getter) ->
                @Suppress("UNCHECKED_CAST")
                number!! to getter as KProperty1<T, Any?>
            }

    private fun oneofProperty(messageClass: KClass<out KtMessage>): (FieldDescriptor, KtMessage) -> Any? {
        val oneofPropertiesSealedClasses =
            messageClass
                .nestedClasses
                .filter { it.isSealed && !it.isSubclassOf(KtEnum::class) }

        val gettersByNumber =
            buildMap {
                oneofPropertiesSealedClasses.forEach { sealedClass ->
                    val oneofPropertyGetter =
                        messageClass.declaredMemberProperties
                            .single { it.returnType.classifier == sealedClass }
                            .let {
                                @Suppress("UNCHECKED_CAST")
                                it as KProperty1<KtMessage, *>
                            }

                    sealedClass.nestedClasses.forEach { sealedClassSubtype ->
                        val (number, getterFromSubtype) = gettersByNumber<Any>(sealedClassSubtype).entries.single()
                        put(number) { msg: KtMessage ->
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

    private fun getUnknownField(
        field: FieldDescriptor,
        message: KtMessage,
    ) = getUnknownFields(message)[field.number.toUInt()]?.let { value ->
        when {
            value.varint.isNotEmpty() ->
                value.varint
                    .map(VarintVal::value)
                    .map {
                        if (field.type == Type.UINT64) {
                            it
                        } else {
                            it.toLong()
                        }
                    }

            value.fixed32.isNotEmpty() ->
                value.fixed32.map(Fixed32Val::value)

            value.fixed64.isNotEmpty() ->
                value.fixed64.map(Fixed64Val::value)

            value.lengthDelimited.isNotEmpty() ->
                value.lengthDelimited
                    .map(LengthDelimitedVal::value)
                    .map {
                        if (field.type == Type.STRING) {
                            StandardCharsets.UTF_8.decode(it.asReadOnlyBuffer()).toString()
                        } else {
                            it
                        }
                    }

            else -> error("unknown field for field number ${field.number} existed but was empty")
        }
    }
        .let {
            if (field.isRepeated) {
                if (field.isMapField) {
                    it ?: emptyMap<Any, Any>()
                } else {
                    it ?: emptyList<Any>()
                }
            } else {
                it?.first()
            }
        }

    fun hasField(message: KtMessage, field: FieldDescriptor): Boolean {
        val value = getField(message, field)

        return if (field.hasPresence()) {
            value != null
        } else {
            value != defaultValue(field)
        }
    }

    fun getField(message: KtMessage, field: FieldDescriptor): Any? =
        reflectedGettersByClass[message::class](field, message)
}

private fun defaultValue(field: FieldDescriptor) =
    when (field.type) {
        Type.UINT64, Type.FIXED64 -> 0uL
        Type.UINT32, Type.FIXED32 -> 0u
        Type.BYTES -> Bytes.empty()
        else -> field.defaultValue
    }

internal fun getUnknownFields(message: KtMessage) =
    message::class
        .declaredMemberProperties
        .firstOrNull { it.returnType.classifier == UnknownFieldSet::class }
        .let {
            @Suppress("UNCHECKED_CAST")
            it as KProperty1<KtMessage, UnknownFieldSet>
        }
        .get(message)
        .unknownFields
