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
 * Base type for generated protobuf enums.
 *
 * Values compare equal when they belong to the same generated enum, have the same wire value, and are
 * either both recognized or both unrecognized. This makes recognized aliases equal while preserving the
 * distinction between a recognized value and an unrecognized value with the same number.
 */
abstract class Enum protected constructor(
    private val enumType: KClass<out Enum>,
    private val isUnrecognized: Boolean,
) {
    abstract val value: Int
    abstract val name: String

    final override fun equals(other: Any?) =
        other is Enum &&
            other.enumType == enumType &&
            other.isUnrecognized == isUnrecognized &&
            other.value == value

    final override fun hashCode(): Int {
        var result = enumType.hashCode()
        result = 31 * result + isUnrecognized.hashCode()
        result = 31 * result + value
        return result
    }

    final override fun toString() =
        name
}
