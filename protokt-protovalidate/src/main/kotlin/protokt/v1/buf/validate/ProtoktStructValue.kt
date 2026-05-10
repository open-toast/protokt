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

package protokt.v1.buf.validate

import com.google.common.primitives.UnsignedLong
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import dev.cel.common.types.CelType
import dev.cel.common.types.StructTypeReference
import dev.cel.common.values.BaseProtoCelValueConverter
import dev.cel.common.values.CelByteString
import dev.cel.common.values.NullValue
import dev.cel.common.values.StructValue
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField
import java.time.Instant
import java.util.Optional

/**
 * A [StructValue] over a protokt [Message]. Handed to CEL as the `this` variable so
 * evaluators can navigate protokt messages directly via reflection, without converting to
 * `com.google.protobuf.DynamicMessage` for the outer message.
 *
 * `value()` returns `this` so that `ProtoCelValueConverter.maybeUnwrap` leaves us as a
 * `StructValue` when selecting nested fields. A subsequent field selection then calls back
 * into `toRuntimeValue`, which recognizes `CelValue` and passes us through unchanged.
 *
 * WKTs split into two groups:
 *  - Concrete types (Duration, Timestamp, wrappers, Empty) have fixed field layouts that map
 *    to a single CEL-native value. We unwrap them natively by reading their fields directly
 *    off the protokt message, with no DynamicMessage allocation and no round-trip through
 *    `BaseProtoCelValueConverter`.
 *  - Open types (Any, Value, Struct, ListValue) wrap arbitrary data: Any holds a packed
 *    message of any type; Value is a union of JSON-like shapes; Struct and ListValue recurse
 *    through Value. We hand these off to [RuntimeContext.convertValue] to produce a
 *    `com.google.protobuf.Message`, then pre-unwrap via
 *    [BaseProtoCelValueConverter.toRuntimeValue]. CEL's maybeUnwrap won't call toRuntimeValue
 *    for us on the way out, so we do it up-front.
 *  - FieldMask is not a WKT for CEL's purposes (see cel-java Patch 2); it flows through the
 *    ordinary [ProtoktStructValue] path like any other message.
 *
 * A null [message] represents a default (unset) message; [find] always returns empty and
 * [select] returns field defaults.
 */
internal class ProtoktStructValue(
    private val message: Message?,
    private val descriptor: Descriptor,
    private val context: RuntimeContext,
) : StructValue<String, ProtoktStructValue>() {

    private val celType: CelType = StructTypeReference.create(descriptor.fullName)

    override fun value(): ProtoktStructValue = this

    override fun celType(): CelType = celType

    override fun isZeroValue(): Boolean = message == null

    override fun select(field: String): Any {
        val fd = findField(field) ?: throw IllegalArgumentException(
            "field '$field' is not declared in message '${descriptor.fullName}'"
        )
        return fieldValue(fd)
    }

    override fun find(field: String): Optional<Any> {
        if (message == null) return Optional.empty()
        val fd = findField(field) ?: return Optional.empty()
        return if (message.hasField(fd)) Optional.of(fieldValue(fd)) else Optional.empty()
    }

    private fun findField(name: String): FieldDescriptor? =
        descriptor.findFieldByName(name) ?: descriptor.fields.firstOrNull { it.jsonName == name }

    private fun fieldValue(fd: FieldDescriptor): Any {
        val raw = message?.getField(fd)
        if (raw == null) {
            return defaultFor(fd)
        }
        return when {
            fd.isMapField -> mapValue(fd, raw as Map<*, *>)
            fd.isRepeated -> (raw as List<*>).map { scalarCelValue(fd, it!!) }
            else -> scalarCelValue(fd, raw)
        }
    }

    private fun mapValue(fd: FieldDescriptor, raw: Map<*, *>): Map<Any, Any> {
        val keyDesc = fd.messageType.findFieldByNumber(1)
        val valDesc = fd.messageType.findFieldByNumber(2)
        return raw.entries.associate { (k, v) ->
            scalarCelValue(keyDesc, k!!) to scalarCelValue(valDesc, v!!)
        }
    }

    private fun scalarCelValue(fd: FieldDescriptor, raw: Any): Any =
        when (raw) {
            is Enum -> raw.value.toLong()
            is Float -> raw.toDouble()
            is Int ->
                if (fd.type == Type.UINT32 || fd.type == Type.FIXED32) {
                    UnsignedLong.valueOf(raw.toLong() and 0xFFFFFFFFL)
                } else {
                    raw.toLong()
                }
            is UInt -> UnsignedLong.valueOf(raw.toLong() and 0xFFFFFFFFL)
            is Long ->
                if (fd.type == Type.UINT64 || fd.type == Type.FIXED64) {
                    UnsignedLong.fromLongBits(raw)
                } else {
                    raw
                }
            is ULong -> UnsignedLong.fromLongBits(raw.toLong())
            is Bytes -> CelByteString.of(raw.bytes)
            is Message -> wrapMessage(raw, fd.messageType, context)
            else -> raw
        }

    private fun defaultFor(fd: FieldDescriptor): Any =
        when {
            fd.isMapField -> emptyMap<Any, Any>()
            fd.isRepeated -> emptyList<Any>()
            fd.type == Type.MESSAGE -> defaultMessage(fd.messageType)
            else -> fd.defaultValue
        }

    private fun defaultMessage(messageType: Descriptor): Any =
        when (messageType.fullName) {
            // Unset wrapper fields are NULL_VALUE per CEL spec.
            in WellKnownTypes.WRAPPER_TYPES -> NullValue.NULL_VALUE
            // Unset concrete WKTs: default values.
            "google.protobuf.Duration" -> java.time.Duration.ZERO
            "google.protobuf.Timestamp" -> Instant.EPOCH
            "google.protobuf.Empty" -> emptyMap<Any, Any>()
            // Open WKTs: let CEL's converter produce the default via DynamicMessage.
            in WellKnownTypes.OPEN_TYPES -> WellKnownTypes.unwrap(
                com.google.protobuf.DynamicMessage.getDefaultInstance(messageType)
            )
            // Non-WKT messages: empty ProtoktStructValue.
            else -> ProtoktStructValue(null, messageType, context)
        }
}

internal object WellKnownTypes {
    /** Wrapper types unwrap to their single `.value` field. Unset wrappers become NULL_VALUE. */
    val WRAPPER_TYPES = setOf(
        "google.protobuf.BoolValue",
        "google.protobuf.BytesValue",
        "google.protobuf.DoubleValue",
        "google.protobuf.FloatValue",
        "google.protobuf.Int32Value",
        "google.protobuf.Int64Value",
        "google.protobuf.StringValue",
        "google.protobuf.UInt32Value",
        "google.protobuf.UInt64Value",
    )

    /** Concrete WKTs unwrap natively. Fixed field layout, fixed CEL-native result. */
    private val CONCRETE_TYPES = WRAPPER_TYPES + setOf(
        "google.protobuf.Duration",
        "google.protobuf.Timestamp",
        "google.protobuf.Empty",
    )

    /** Open WKTs wrap arbitrary data. We hand them off to cel-java's converter via DynamicMessage. */
    val OPEN_TYPES = setOf(
        "google.protobuf.Any",
        "google.protobuf.Value",
        "google.protobuf.Struct",
        "google.protobuf.ListValue",
    )

    private val converter: BaseProtoCelValueConverter = object : BaseProtoCelValueConverter() {}

    fun unwrap(dynamicMessage: Any): Any = converter.toRuntimeValue(dynamicMessage)

    fun isConcrete(typeName: String): Boolean = typeName in CONCRETE_TYPES
    fun isOpen(typeName: String): Boolean = typeName in OPEN_TYPES
}

internal fun wrapMessage(
    raw: Message,
    messageType: Descriptor,
    context: RuntimeContext,
): Any {
    val fullName = messageType.fullName
    return when {
        WellKnownTypes.isConcrete(fullName) -> unwrapConcreteWkt(raw, messageType)
        WellKnownTypes.isOpen(fullName) -> WellKnownTypes.unwrap(context.convertValue(raw))
        else -> ProtoktStructValue(raw, messageType, context)
    }
}

private fun unwrapConcreteWkt(raw: Message, messageType: Descriptor): Any {
    // Each concrete WKT has a known field layout; we read fields off the protokt message by
    // number and map to the CEL-native form directly.
    fun field(number: Int): Any? = raw.getField(messageType.findFieldByNumber(number))
    return when (messageType.fullName) {
        "google.protobuf.Duration" ->
            java.time.Duration.ofSeconds(field(1) as? Long ?: 0L, (field(2) as? Int ?: 0).toLong())
        "google.protobuf.Timestamp" ->
            Instant.ofEpochSecond(field(1) as? Long ?: 0L, (field(2) as? Int ?: 0).toLong())
        "google.protobuf.Empty" -> emptyMap<Any, Any>()
        "google.protobuf.BoolValue" -> field(1) as? Boolean ?: false
        "google.protobuf.StringValue" -> field(1) as? String ?: ""
        "google.protobuf.BytesValue" -> CelByteString.of((field(1) as? Bytes ?: Bytes.empty()).bytes)
        "google.protobuf.DoubleValue" -> field(1) as? Double ?: 0.0
        "google.protobuf.FloatValue" -> (field(1) as? Float ?: 0.0f).toDouble()
        "google.protobuf.Int32Value" -> (field(1) as? Int ?: 0).toLong()
        "google.protobuf.Int64Value" -> field(1) as? Long ?: 0L
        "google.protobuf.UInt32Value" ->
            UnsignedLong.valueOf(((field(1) as? UInt)?.toLong() ?: 0L) and 0xFFFFFFFFL)
        "google.protobuf.UInt64Value" ->
            UnsignedLong.fromLongBits((field(1) as? ULong)?.toLong() ?: 0L)
        else -> error("unhandled concrete WKT: ${messageType.fullName}")
    }
}

