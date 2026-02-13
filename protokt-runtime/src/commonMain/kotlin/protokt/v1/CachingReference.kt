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
class CachingReference<WireT : Any, KotlinT : Any>(
    @Volatile private var ref: Any,
    private val converter: CachingConverter<WireT, KotlinT>
) {
    /** Returns the user-facing Kotlin type. Lazily converts from wire form if needed. */
    fun value(): KotlinT {
        val current = ref
        return if (converter.wrapperClass.isInstance(current)) {
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
        return if (!converter.wrapperClass.isInstance(current)) {
            @Suppress("UNCHECKED_CAST")
            current as WireT
        } else {
            @Suppress("UNCHECKED_CAST")
            val converted = converter.unwrap(current as KotlinT)
            ref = converted
            converted
        }
    }

    /** Write to a Writer using whichever form is currently cached. No forced conversion. */
    fun writeTo(writer: Writer) =
        converter.writeTo(writer, ref)

    /** Compute serialized size from whichever form is currently cached. */
    fun sizeOf(): Int =
        converter.sizeOf(ref)

    /** Check default-ness without conversion. */
    fun isDefault(): Boolean =
        converter.isDefault(ref)

    fun isNotDefault(): Boolean =
        !isDefault()

    override fun equals(other: Any?): Boolean =
        other is CachingReference<*, *> && value() == other.value()

    override fun hashCode(): Int =
        value().hashCode()

    override fun toString(): String =
        value().toString()
}
