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

import kotlin.concurrent.Volatile

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class LazyReference<WireT : Any, KotlinT : Any>(
    @Volatile private var ref: Any,
    private val converter: Converter<WireT, KotlinT>
) {
    /** Returns the user-facing Kotlin type. Lazily converts from wire form if needed. */
    fun value(): KotlinT {
        val current = ref
        return if (converter.wrapper.isInstance(current)) {
            @Suppress("UNCHECKED_CAST")
            current as KotlinT
        } else {
            @Suppress("UNCHECKED_CAST")
            val converted = converter.wrap(current as WireT)
            ref = converted
            converted
        }
    }

    /** Returns the wire type. Lazily converts from Kotlin form if needed. */
    fun wireValue(): WireT {
        val current = ref
        return if (!converter.wrapper.isInstance(current)) {
            @Suppress("UNCHECKED_CAST")
            current as WireT
        } else {
            @Suppress("UNCHECKED_CAST")
            val converted = converter.unwrap(current as KotlinT)
            ref = converted
            converted
        }
    }

    /** Check default-ness from the wire form. */
    fun isDefault(): Boolean =
        when (val wire = wireValue()) {
            is Bytes -> wire.isEmpty()
            is String -> wire.isEmpty()
            is Boolean -> !wire
            is Int -> wire == 0
            is UInt -> wire == 0u
            is Long -> wire == 0L
            is ULong -> wire == 0uL
            is Float -> wire == 0.0f
            is Double -> wire == 0.0
            else -> false
        }

    fun isNotDefault(): Boolean =
        !isDefault()

    override fun equals(other: Any?): Boolean =
        other is LazyReference<*, *> && value() == other.value()

    override fun hashCode(): Int =
        value().hashCode()

    override fun toString(): String =
        value().toString()
}
