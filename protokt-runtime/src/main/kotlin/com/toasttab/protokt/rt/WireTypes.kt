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

interface Serialized {
    val wireFormat: Int
}

interface WireType0 : Serialized {
    override val wireFormat
        get() = 0
}

interface WireType1 : Serialized {
    override val wireFormat
        get() = 1
}

interface WireType2 : Serialized {
    override val wireFormat
        get() = 2
}

interface WireType5 : Serialized {
    override val wireFormat
        get() = 5
}

interface DelegatingSerialized : Serialized {
    val wireType: Serialized

    override val wireFormat
        get() = wireType.wireFormat
}

interface InstanceWithWireType0 : DelegatingSerialized {
    override val wireType
        get() = InstanceWithWireType0

    companion object : WireType0
}

interface InstanceWithWireType1 : DelegatingSerialized {
    override val wireType
        get() = InstanceWithWireType1

    companion object : WireType1
}

interface InstanceWithWireType2 : DelegatingSerialized {
    override val wireType
        get() = InstanceWithWireType2

    companion object : WireType2
}

interface InstanceWithWireType5 : DelegatingSerialized {
    override val wireType
        get() = InstanceWithWireType5

    companion object : WireType5
}
