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

@file:Suppress("DEPRECATION")

package com.toasttab.protokt

import com.toasttab.protokt.rt.Bytes
import com.toasttab.protokt.rt.KtMessage
import kotlin.reflect.KClass

@Deprecated("use v1")
fun Any.Deserializer.pack(
    msg: KtMessage,
    typeUrlPrefix: String = "type.googleapis.com"
) =
    Any {
        typeUrl = typeUrl(typeUrlPrefix, msg)
        value = Bytes(msg.serialize())
    }

private fun typeUrl(typeUrlPrefix: String, msg: KtMessage) =
    if (typeUrlPrefix.endsWith("/")) {
        typeUrlPrefix
    } else {
        "$typeUrlPrefix/"
    } + fullTypeName(msg::class)

@Deprecated("use v1")
inline fun <reified T : protokt.v1.Message> Any.isA() =
    typeUrl.substringAfterLast('/') ==
        (
            T::class.java.getAnnotation(protokt.v1.GeneratedMessage::class.java)?.fullTypeName
                ?: T::class.java.getAnnotation(com.toasttab.protokt.rt.KtGeneratedMessage::class.java)?.fullTypeName
                ?: error("class ${T::class} has no protokt generated message annotation")
            )

private fun fullTypeName(klass: KClass<*>) =
    klass.java.getAnnotation(protokt.v1.GeneratedMessage::class.java)?.fullTypeName
        ?: klass.java.getAnnotation(com.toasttab.protokt.rt.KtGeneratedMessage::class.java)?.fullTypeName
        ?: error("class $klass has no protokt generated message annotation")
