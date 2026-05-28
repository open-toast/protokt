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

@Beta
sealed interface ExtensionCodec<T> {
    fun decodeSingular(field: UnknownFieldSet.Field): T?
    fun decodeRepeated(field: UnknownFieldSet.Field): List<T>
    fun encode(fieldNumber: UInt, value: T): UnknownField
}

@Beta
object ExtensionCodecs {
    val int32: ExtensionCodec<Int> =
        VarintCodec({ it.toLong().toInt() }, { it.toLong().toULong() })

    val int64: ExtensionCodec<Long> =
        VarintCodec({ it.toLong() }, { it.toULong() })

    val uint32: ExtensionCodec<UInt> =
        VarintCodec({ it.toUInt() }, { it.toULong() })

    val uint64: ExtensionCodec<ULong> =
        VarintCodec({ it }, { it })

    val sint32: ExtensionCodec<Int> =
        VarintCodec(
            {
                val n = it.toInt()
                (n ushr 1) xor -(n and 1)
            },
            {
                val v = it
                ((v shl 1) xor (v shr 31)).toUInt().toULong()
            }
        )

    val sint64: ExtensionCodec<Long> =
        VarintCodec(
            {
                val n = it.toLong()
                (n ushr 1) xor -(n and 1)
            },
            {
                val v = it
                ((v shl 1) xor (v shr 63)).toULong()
            }
        )

    val bool: ExtensionCodec<Boolean> =
        VarintCodec({ it != 0uL }, { if (it) 1uL else 0uL })

    val fixed32: ExtensionCodec<UInt> =
        Fixed32Codec({ it }, { it })

    val sfixed32: ExtensionCodec<Int> =
        Fixed32Codec({ it.toInt() }, { it.toUInt() })

    val float: ExtensionCodec<Float> =
        Fixed32Codec({ Float.fromBits(it.toInt()) }, { it.toRawBits().toUInt() })

    val fixed64: ExtensionCodec<ULong> =
        Fixed64Codec({ it }, { it })

    val sfixed64: ExtensionCodec<Long> =
        Fixed64Codec({ it.toLong() }, { it.toULong() })

    val double: ExtensionCodec<Double> =
        Fixed64Codec({ Double.fromBits(it.toLong()) }, { it.toRawBits().toULong() })

    val string: ExtensionCodec<String> =
        LengthDelimitedCodec(
            { it.value.value.decodeToString() },
            { LengthDelimitedVal(Bytes(it.encodeToByteArray())) }
        )

    val bytes: ExtensionCodec<Bytes> =
        LengthDelimitedCodec(
            { it.value },
            { LengthDelimitedVal(it) }
        )

    fun <T : Message> message(deserializer: Deserializer<T>): ExtensionCodec<T> =
        LengthDelimitedCodec(
            { deserializer.deserialize(it.value) },
            { LengthDelimitedVal(Bytes(it.serialize())) }
        )

    fun <T : Enum> enum(deserializer: EnumDeserializer<T>): ExtensionCodec<T> =
        VarintCodec({ deserializer.deserialize(it.toInt()) }, { it.value.toULong() })
}

private class VarintCodec<T>(
    private val decode: (ULong) -> T,
    private val encode: (T) -> ULong
) : ExtensionCodec<T> {
    override fun decodeSingular(field: UnknownFieldSet.Field): T? =
        field.varint.lastOrNull()?.let { decode(it.value) }

    override fun decodeRepeated(field: UnknownFieldSet.Field): List<T> =
        field.varint.map { decode(it.value) }

    override fun encode(fieldNumber: UInt, value: T): UnknownField =
        UnknownField.varint(fieldNumber, encode(value).toLong())
}

private class Fixed32Codec<T>(
    private val decode: (UInt) -> T,
    private val encode: (T) -> UInt
) : ExtensionCodec<T> {
    override fun decodeSingular(field: UnknownFieldSet.Field): T? =
        field.fixed32.lastOrNull()?.let { decode(it.value) }

    override fun decodeRepeated(field: UnknownFieldSet.Field): List<T> =
        field.fixed32.map { decode(it.value) }

    override fun encode(fieldNumber: UInt, value: T): UnknownField =
        UnknownField.fixed32(fieldNumber, encode(value))
}

private class Fixed64Codec<T>(
    private val decode: (ULong) -> T,
    private val encode: (T) -> ULong
) : ExtensionCodec<T> {
    override fun decodeSingular(field: UnknownFieldSet.Field): T? =
        field.fixed64.lastOrNull()?.let { decode(it.value) }

    override fun decodeRepeated(field: UnknownFieldSet.Field): List<T> =
        field.fixed64.map { decode(it.value) }

    override fun encode(fieldNumber: UInt, value: T): UnknownField =
        UnknownField.fixed64(fieldNumber, encode(value))
}

private class LengthDelimitedCodec<T>(
    private val decode: (LengthDelimitedVal) -> T,
    private val encode: (T) -> LengthDelimitedVal
) : ExtensionCodec<T> {
    override fun decodeSingular(field: UnknownFieldSet.Field): T? =
        field.lengthDelimited.lastOrNull()?.let(decode)

    override fun decodeRepeated(field: UnknownFieldSet.Field): List<T> =
        field.lengthDelimited.map(decode)

    override fun encode(fieldNumber: UInt, value: T): UnknownField =
        UnknownField.lengthDelimited(fieldNumber, encode(value).value.value)
}
