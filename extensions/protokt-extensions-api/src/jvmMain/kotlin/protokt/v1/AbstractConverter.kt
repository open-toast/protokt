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

package protokt.v1

import java.lang.reflect.ParameterizedType
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

abstract class AbstractConverter<ProtobufT : Any, KotlinT : Any> : Converter<ProtobufT, KotlinT> {
    final override val wrapper: KClass<KotlinT>
        get() = resolveType(1)

    final override val wrapped: KClass<ProtobufT>
        get() = resolveType(0)
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Any.resolveType(index: Int) =
    Reflection.getOrCreateKotlinClass(
        (this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[index] as Class<T>
    ) as KClass<T>
