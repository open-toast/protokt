/*
 * Copyright (c) 2019. Toast Inc.
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

package com.toasttab.protokt.rt

interface UnknownValue {
    fun size(): Int
}

inline class LengthDelimitedVal(val value: ByteArray) : UnknownValue {
    override fun size() = sizeof(value)
}

inline class VarIntVal(val value: Long) : UnknownValue {
    override fun size() = sizeof(UInt64(value))
}

inline class Fixed32Val(val value: Fixed32) : UnknownValue {
    override fun size() = sizeof(value)
}

inline class Fixed64Val(val value: Fixed64) : UnknownValue {
    override fun size() = sizeof(value)
}

inline class ListVal(val value: List<UnknownValue>) : UnknownValue {
    override fun size() = value.sumBy { it.size() }
}

@Suppress("UNUSED")
data class Unknown(val fieldNum: Int, val value: UnknownValue) {
    constructor(fieldNum: Int, v: Long, fixed: Boolean = false) :
        this(
            fieldNum,
            if (fixed) Fixed64Val(Fixed64(v)) else VarIntVal(v))

    constructor(fieldNum: Int, v: Int, fixed: Boolean = false) :
        this(
            fieldNum,
            if (fixed) Fixed32Val(Fixed32(v))
            else VarIntVal(v.toLong()))

    constructor(fieldNum: Int, ba: ByteArray) :
        this(fieldNum, LengthDelimitedVal(ba))

    constructor(fieldNum: Int, str: String) :
        this(fieldNum, str.toByteArray())

    fun sizeof() = when (value) {
        is ListVal -> (sizeof(Tag(fieldNum)) * value.value.size) + value.size()
        else -> sizeof(Tag(fieldNum)) + value.size()
    }
}
