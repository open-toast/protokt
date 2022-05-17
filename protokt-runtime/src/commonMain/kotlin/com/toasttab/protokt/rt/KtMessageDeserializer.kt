/*
 * Copyright (c) 2019 Toast Inc.
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

interface KtMessageDeserializer {
    fun readBool(): Boolean
    fun readBytes(): Bytes
    fun readBytesSlice(): BytesSlice
    fun readDouble(): Double
    fun readFixed32(): Int
    fun readFixed64(): Long
    fun readFloat(): Float
    fun readInt32(): Int
    fun readInt64(): Long
    fun readSFixed32(): Int
    fun readSFixed64(): Long
    fun readSInt32(): Int
    fun readSInt64(): Long
    fun readString(): String
    fun readUInt32(): Int
    fun readUInt64(): Long
    fun readTag(): Int
    fun readUnknown(): UnknownField
    fun readRepeated(packed: Boolean, acc: KtMessageDeserializer.() -> Unit)
    fun <T : KtEnum> readEnum(e: KtEnumDeserializer<T>): T
    fun <T : KtMessage> readMessage(m: KtDeserializer<T>): T
}
