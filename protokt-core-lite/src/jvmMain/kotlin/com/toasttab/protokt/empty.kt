/*
 * Copyright (c) 2023 Toast, Inc.
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

@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/protobuf/empty.proto
package com.toasttab.protokt

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.UnknownFieldSet
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

/**
 * A generic empty message that you can re-use to avoid defining duplicated empty messages in your
 * APIs. A typical example is to use it as the request or the response type of an API method. For
 * instance:
 *
 *      service Foo {       rpc Bar(google.protobuf.Empty) returns (google.protobuf.Empty);     }
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Empty")
class Empty private constructor(
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int = unknownFields.size()

    override fun serialize(serializer: KtMessageSerializer) {
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
        other is Empty && other.unknownFields == unknownFields

    override fun hashCode(): Int = unknownFields.hashCode()

    override fun toString(): String = "Empty(${if (unknownFields.isEmpty()) "" else
        "unknownFields=$unknownFields"})"

    fun copy(dsl: EmptyDsl.() -> Unit): Empty = Empty.Deserializer {
        unknownFields = this@Empty.unknownFields
        dsl()
    }

    class EmptyDsl {
        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Empty = Empty(unknownFields)
    }

    companion object Deserializer : KtDeserializer<Empty>, (EmptyDsl.() -> Unit) -> Empty {
        override fun deserialize(deserializer: KtMessageDeserializer): Empty {
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Empty(UnknownFieldSet.from(unknownFields))
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: EmptyDsl.() -> Unit): Empty = EmptyDsl().apply(dsl).build()
    }
}
