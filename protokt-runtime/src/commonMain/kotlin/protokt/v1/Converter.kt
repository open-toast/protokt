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

package protokt.v1

import kotlin.reflect.KClass

/**
 * Converts between a protobuf wire value and its user-facing value.
 */
interface Converter<WireT : Any, ValueT : Any> {
    /**
     * The protobuf wire type. It must be runtime-disjoint from [valueType].
     */
    val wireType: KClass<WireT>

    /**
     * The user-facing type. It must be runtime-disjoint from [wireType].
     */
    val valueType: KClass<ValueT>

    fun wrap(unwrapped: WireT): ValueT
    fun unwrap(wrapped: ValueT): WireT
}
