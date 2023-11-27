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
// Source: protokt/ext/inet_socket_address.proto
package com.toasttab.protokt.ext

import com.toasttab.protokt.rt.Int32
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import java.net.InetAddress
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

@KtGeneratedMessage("protokt.ext.InetSocketAddress")
@Deprecated("use v1")
class InetSocketAddress private constructor(
    val address: InetAddress,
    val port: Int,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (com.toasttab.protokt.ext.InetAddressConverter.unwrap(address).isNotEmpty()) {
            result += sizeof(Tag(1)) +
                    sizeof(com.toasttab.protokt.ext.InetAddressConverter.unwrap(address))
        }
        if (port != 0) {
            result += sizeof(Tag(2)) + sizeof(Int32(port))
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (com.toasttab.protokt.ext.InetAddressConverter.unwrap(address).isNotEmpty()) {
            serializer.write(Tag(10)).write(com.toasttab.protokt.ext.InetAddressConverter.unwrap(address))
        }
        if (port != 0) {
            serializer.write(Tag(16)).write(Int32(port))
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is InetSocketAddress &&
            other.address == address &&
            other.port == port &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + port.hashCode()
        return result
    }

    override fun toString(): String = "InetSocketAddress(" +
            "address=$address, " +
            "port=$port" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: InetSocketAddressDsl.() -> Unit): InetSocketAddress =
        InetSocketAddress.Deserializer {
            address = this@InetSocketAddress.address
            port = this@InetSocketAddress.port
            unknownFields = this@InetSocketAddress.unknownFields
            dsl()
        }

    class InetSocketAddressDsl {
        var address: InetAddress? = null

        var port: Int = 0

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): InetSocketAddress = InetSocketAddress(address ?:
        com.toasttab.protokt.ext.InetAddressConverter.wrap(com.toasttab.protokt.rt.Bytes.empty().bytes),
            port,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<InetSocketAddress>,
            (InetSocketAddressDsl.() -> Unit) -> InetSocketAddress {
        override fun deserialize(deserializer: KtMessageDeserializer): InetSocketAddress {
            var address : InetAddress? = null
            var port = 0
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return InetSocketAddress(address ?:
                    com.toasttab.protokt.ext.InetAddressConverter.wrap(com.toasttab.protokt.rt.Bytes.empty().bytes),
                        port,
                        UnknownFieldSet.from(unknownFields))
                    10 -> address =
                        com.toasttab.protokt.ext.InetAddressConverter.wrap(deserializer.readBytes().bytes)
                    16 -> port = deserializer.readInt32()
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: InetSocketAddressDsl.() -> Unit): InetSocketAddress =
            InetSocketAddressDsl().apply(dsl).build()
    }
}
