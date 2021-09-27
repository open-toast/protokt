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

package com.toasttab.protokt

import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

fun Any.Deserializer.pack(
    msg: KtMessage,
    typeUrlPrefix: String = "type.googleapis.com"
) =
    Any {
        typeUrl = typeUrl(typeUrlPrefix, msg)
        value = Bytes(msg.serialize())
    }

private fun typeUrl(typeUrlPrefix: String, msg: KtMessage): String {
    val prefix =
        if (typeUrlPrefix.endsWith("/")) {
            typeUrlPrefix
        } else {
            "$typeUrlPrefix/"
        }

    @Suppress("DEPRECATION")
    val typeNameFromAnnotation =
        msg::class.findAnnotation<KtGeneratedMessage>()
            ?.fullTypeName
            ?.takeIf { it.isNotEmpty() }

    return if (typeNameFromAnnotation != null) {
        prefix + typeNameFromAnnotation
    } else {
        val deserializer = msg::class.companionObjectInstance!!
        val typeNameFromDescriptor =
            deserializer::class.declaredMemberProperties
                .single { it.name == "descriptor" }
                .let {
                    @Suppress("UNCHECKED_CAST")
                    it as KProperty1<kotlin.Any, Descriptor>
                }
                .get(deserializer)
                .fullName

        prefix + typeNameFromDescriptor
    }
}

inline fun <reified T : KtMessage> Any.unpack(deserializer: KtDeserializer<T>): T {
    require(isA<T>()) {
        "Type $typeUrl of the Any message does not match the given " +
            "deserializer ${deserializer::class.qualifiedName}"
    }

    return deserializer.deserialize(value)
}

@Suppress("DEPRECATION")
inline fun <reified T : KtMessage> Any.isA() =
    typeUrl.substringAfterLast('/') ==
        (
            T::class.findAnnotation<KtGeneratedMessage>()
                ?.fullTypeName
                ?.takeIf { it.isNotEmpty() }
                ?: T::class.companionObjectInstance!!.let { deserializer ->
                    deserializer::class.declaredMemberProperties
                        .single { it.name == "descriptor" }
                        .let {
                            @Suppress("UNCHECKED_CAST")
                            it as KProperty1<kotlin.Any, Descriptor>
                        }
                        .get(deserializer)
                        .fullName
                }
            )
