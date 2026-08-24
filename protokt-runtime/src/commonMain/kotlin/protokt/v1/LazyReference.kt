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

/**
 * Lazily converts between runtime-disjoint wire and value types while retaining one representation at a time.
 */
@OnlyForUseByGeneratedProtoCode
class LazyReference<WireT : Any, ValueT : Any>(
    @Volatile private var ref: Any,
    private val converter: Converter<WireT, ValueT>
) {
    init {
        representation(ref)
    }

    /** Returns the user-facing Kotlin type. Lazily converts from wire form if needed. */
    fun value(): ValueT {
        val current = ref
        return if (converter.valueType.isInstance(current)) {
            @Suppress("UNCHECKED_CAST")
            current as ValueT
        } else {
            @Suppress("UNCHECKED_CAST")
            val converted = converter.wrap(current as WireT)
            require(representation(converted) == Representation.Value) {
                "Converter returned a value that does not exclusively match ${converter.valueType}"
            }
            ref = converted
            converted
        }
    }

    /** Returns the wire type. Lazily converts from Kotlin form if needed. */
    fun wireValue(): WireT {
        val current = ref
        return if (converter.wireType.isInstance(current)) {
            @Suppress("UNCHECKED_CAST")
            current as WireT
        } else {
            @Suppress("UNCHECKED_CAST")
            val converted = converter.unwrap(current as ValueT)
            require(representation(converted) == Representation.Wire) {
                "Converter returned a value that does not exclusively match ${converter.wireType}"
            }
            ref = converted
            converted
        }
    }

    override fun equals(other: Any?): Boolean =
        other is LazyReference<*, *> && wireValue() == other.wireValue()

    override fun hashCode(): Int =
        wireValue().hashCode()

    override fun toString(): String =
        value().toString()

    private fun representation(value: Any): Representation {
        val isWire = converter.wireType.isInstance(value)
        val isValue = converter.valueType.isInstance(value)

        require(isWire != isValue) {
            "Converter value ${value::class} must match exactly one of ${converter.wireType} and ${converter.valueType}"
        }

        return if (isWire) Representation.Wire else Representation.Value
    }

    private enum class Representation {
        Wire,
        Value
    }
}
