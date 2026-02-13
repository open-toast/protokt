/*
 * Copyright (c) 2026 Toast, Inc.
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

@OnlyForUseByGeneratedProtoCode
interface CachingConverter<WireT : Any, KotlinT : Any> {
    val wrapperClass: KClass<KotlinT>

    fun wrap(unwrapped: WireT): KotlinT
    fun unwrap(wrapped: KotlinT): WireT

    /** Write whichever form is currently cached, without forcing conversion. */
    fun writeTo(writer: Writer, value: Any)

    /** Compute serialized size from whichever form is currently cached. */
    fun sizeOf(value: Any): Int

    /** Check emptiness/default-ness from whichever form is currently cached. */
    fun isDefault(value: Any): Boolean
}
