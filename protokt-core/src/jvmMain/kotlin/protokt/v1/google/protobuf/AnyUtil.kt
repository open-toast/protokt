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

@file:JvmName("AnyUtil")

package protokt.v1.google.protobuf

import protokt.v1.Bytes
import protokt.v1.KtDeserializer
import protokt.v1.KtGeneratedMessage
import protokt.v1.KtMessage
import kotlin.reflect.KClass

@JvmOverloads
fun Any.Deserializer.pack(
    msg: KtMessage,
    typeUrlPrefix: String = "type.googleapis.com"
) =
    Any {
        typeUrl = typeUrl(typeUrlPrefix, msg)
        value = Bytes.from(msg)
    }

private fun typeUrl(typeUrlPrefix: String, msg: KtMessage) =
    if (typeUrlPrefix.endsWith("/")) {
        typeUrlPrefix
    } else {
        "$typeUrlPrefix/"
    } + fullTypeName(msg::class)

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
            T::class.java.getAnnotation(KtGeneratedMessage::class.java)?.fullTypeName
                ?: T::class.java.getAnnotation(com.toasttab.protokt.rt.KtGeneratedMessage::class.java)?.fullTypeName
                ?: error("class ${T::class} has no protokt generated message annotation")
            )

@Suppress("DEPRECATION")
private fun fullTypeName(klass: KClass<*>) =
    klass.java.getAnnotation(KtGeneratedMessage::class.java)?.fullTypeName
        ?: klass.java.getAnnotation(com.toasttab.protokt.rt.KtGeneratedMessage::class.java)?.fullTypeName
        ?: error("class $klass has no protokt generated message annotation")
