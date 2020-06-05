/*
 * Copyright (c) 2020 Toast Inc.
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

interface Boxed {
    val value: Number
}

interface BoxedType0 : InstanceWithWireType0, Boxed
interface BoxedType1 : InstanceWithWireType1, Boxed
interface BoxedType5 : InstanceWithWireType5, Boxed

inline class Int32(override val value: Int) : BoxedType0
inline class Int64(override val value: Long) : BoxedType0
inline class SInt32(override val value: Int) : BoxedType0
inline class SInt64(override val value: Long) : BoxedType0
inline class UInt32(override val value: Int) : BoxedType0
inline class UInt64(override val value: Long) : BoxedType0

inline class Fixed64(override val value: Long) : BoxedType1
inline class SFixed64(override val value: Long) : BoxedType1

inline class Fixed32(override val value: Int) : BoxedType5
inline class SFixed32(override val value: Int) : BoxedType5
