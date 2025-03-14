@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/protobuf/type.proto
package com.toasttab.protokt

import com.toasttab.protokt.rt.Int32
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.copyList
import com.toasttab.protokt.rt.finishList
import com.toasttab.protokt.rt.sizeof
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList

/**
 * A protocol buffer message type.
 */
@Deprecated("for backwards compatibility only")
sealed class Syntax(
    override val `value`: Int,
    override val name: String,
) : KtEnum() {
    /**
     * The fully qualified message name.
     */
    object PROTO2 : Syntax(0, "PROTO2")

    /**
     * The list of fields.
     */
    object PROTO3 : Syntax(1, "PROTO3")

    class UNRECOGNIZED(
        `value`: Int,
    ) : Syntax(value, "UNRECOGNIZED")

    companion object Deserializer : KtEnumDeserializer<Syntax> {
        override fun from(`value`: Int): Syntax = when (value) {
            0 -> PROTO2
            1 -> PROTO3
            else -> UNRECOGNIZED(value)
        }
    }
}

/**
 * A protocol buffer message type.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Type")
class Type private constructor(
    /**
     * The fully qualified message name.
     */
    val name: String,
    /**
     * The list of fields.
     */
    val fields: List<Field>,
    /**
     * The list of types appearing in `oneof` definitions in this type.
     */
    val oneofs: List<String>,
    /**
     * The protocol buffer options.
     */
    val options: List<Option>,
    /**
     * The source context.
     */
    val sourceContext: SourceContext?,
    /**
     * The source syntax.
     */
    val syntax: Syntax,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (fields.isNotEmpty()) {
            result += (sizeof(Tag(2)) * fields.size) + fields.sumOf { sizeof(it) }
        }
        if (oneofs.isNotEmpty()) {
            result += (sizeof(Tag(3)) * oneofs.size) + oneofs.sumOf { sizeof(it) }
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(4)) * options.size) + options.sumOf { sizeof(it) }
        }
        if (sourceContext  != null) {
            result += sizeof(Tag(5)) + sizeof(sourceContext)
        }
        if (syntax.value != 0) {
            result += sizeof(Tag(6)) + sizeof(syntax)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (fields.isNotEmpty()) {
            fields.forEach { serializer.write(Tag(18)).write(it) }
        }
        if (oneofs.isNotEmpty()) {
            oneofs.forEach { serializer.write(Tag(26)).write(it) }
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(34)).write(it) }
        }
        if (sourceContext  != null) {
            serializer.write(Tag(42)).write(sourceContext)
        }
        if (syntax.value != 0) {
            serializer.write(Tag(48)).write(syntax)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: kotlin.Any?): Boolean = other is Type &&
            other.name == name &&
            other.fields == fields &&
            other.oneofs == oneofs &&
            other.options == options &&
            other.sourceContext == sourceContext &&
            other.syntax == syntax &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + fields.hashCode()
        result = 31 * result + oneofs.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + sourceContext.hashCode()
        result = 31 * result + syntax.hashCode()
        return result
    }

    override fun toString(): String = "Type(" +
            "name=$name, " +
            "fields=$fields, " +
            "oneofs=$oneofs, " +
            "options=$options, " +
            "sourceContext=$sourceContext, " +
            "syntax=$syntax" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: TypeDsl.() -> Unit): Type = Type.Deserializer {
        name = this@Type.name
        fields = this@Type.fields
        oneofs = this@Type.oneofs
        options = this@Type.options
        sourceContext = this@Type.sourceContext
        syntax = this@Type.syntax
        unknownFields = this@Type.unknownFields
        dsl()
    }

    class TypeDsl {
        var name: String = ""

        var fields: List<Field> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var oneofs: List<String> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var sourceContext: SourceContext? = null

        var syntax: Syntax = Syntax.from(0)

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Type = Type(name,
            finishList(fields),
            finishList(oneofs),
            finishList(options),
            sourceContext,
            syntax,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Type>, (TypeDsl.() -> Unit) -> Type {
        override fun deserialize(deserializer: KtMessageDeserializer): Type {
            var name = ""
            var fields : MutableList<Field>? = null
            var oneofs : MutableList<String>? = null
            var options : MutableList<Option>? = null
            var sourceContext : SourceContext? = null
            var syntax = Syntax.from(0)
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Type(name,
                        finishList(fields),
                        finishList(oneofs),
                        finishList(options),
                        sourceContext,
                        syntax,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> fields = (fields ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Field))
                        }
                    }
                    26 -> oneofs = (oneofs ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readString())
                        }
                    }
                    34 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    42 -> sourceContext =
                        deserializer.readMessage(com.toasttab.protokt.SourceContext)
                    48 -> syntax = deserializer.readEnum(com.toasttab.protokt.Syntax)
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: TypeDsl.() -> Unit): Type = TypeDsl().apply(dsl).build()
    }
}

/**
 * A single field of a message type.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Field")
class Field private constructor(
    /**
     * The field type.
     */
    val kind: Kind,
    /**
     * The field cardinality.
     */
    val cardinality: Cardinality,
    /**
     * The field number.
     */
    val number: Int,
    /**
     * The field name.
     */
    val name: String,
    /**
     * The field type URL, without the scheme, for message or enumeration types. Example:
     * `"type.googleapis.com/google.protobuf.Timestamp"`.
     */
    val typeUrl: String,
    /**
     * The index of the field type in `Type.oneofs`, for message or enumeration types. The first
     * type has index 1; zero means the type is not in the list.
     */
    val oneofIndex: Int,
    /**
     * Whether to use alternative packed wire representation.
     */
    val packed: Boolean,
    /**
     * The protocol buffer options.
     */
    val options: List<Option>,
    /**
     * The field JSON name.
     */
    val jsonName: String,
    /**
     * The string value of the default value of this field. Proto2 syntax only.
     */
    val defaultValue: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (kind.value != 0) {
            result += sizeof(Tag(1)) + sizeof(kind)
        }
        if (cardinality.value != 0) {
            result += sizeof(Tag(2)) + sizeof(cardinality)
        }
        if (number != 0) {
            result += sizeof(Tag(3)) + sizeof(Int32(number))
        }
        if (name.isNotEmpty()) {
            result += sizeof(Tag(4)) + sizeof(name)
        }
        if (typeUrl.isNotEmpty()) {
            result += sizeof(Tag(6)) + sizeof(typeUrl)
        }
        if (oneofIndex != 0) {
            result += sizeof(Tag(7)) + sizeof(Int32(oneofIndex))
        }
        if (packed) {
            result += sizeof(Tag(8)) + sizeof(packed)
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(9)) * options.size) + options.sumOf { sizeof(it) }
        }
        if (jsonName.isNotEmpty()) {
            result += sizeof(Tag(10)) + sizeof(jsonName)
        }
        if (defaultValue.isNotEmpty()) {
            result += sizeof(Tag(11)) + sizeof(defaultValue)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (kind.value != 0) {
            serializer.write(Tag(8)).write(kind)
        }
        if (cardinality.value != 0) {
            serializer.write(Tag(16)).write(cardinality)
        }
        if (number != 0) {
            serializer.write(Tag(24)).write(Int32(number))
        }
        if (name.isNotEmpty()) {
            serializer.write(Tag(34)).write(name)
        }
        if (typeUrl.isNotEmpty()) {
            serializer.write(Tag(50)).write(typeUrl)
        }
        if (oneofIndex != 0) {
            serializer.write(Tag(56)).write(Int32(oneofIndex))
        }
        if (packed) {
            serializer.write(Tag(64)).write(packed)
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(74)).write(it) }
        }
        if (jsonName.isNotEmpty()) {
            serializer.write(Tag(82)).write(jsonName)
        }
        if (defaultValue.isNotEmpty()) {
            serializer.write(Tag(90)).write(defaultValue)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: kotlin.Any?): Boolean = other is Field &&
            other.kind == kind &&
            other.cardinality == cardinality &&
            other.number == number &&
            other.name == name &&
            other.typeUrl == typeUrl &&
            other.oneofIndex == oneofIndex &&
            other.packed == packed &&
            other.options == options &&
            other.jsonName == jsonName &&
            other.defaultValue == defaultValue &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + kind.hashCode()
        result = 31 * result + cardinality.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + typeUrl.hashCode()
        result = 31 * result + oneofIndex.hashCode()
        result = 31 * result + packed.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + jsonName.hashCode()
        result = 31 * result + defaultValue.hashCode()
        return result
    }

    override fun toString(): String = "Field(" +
            "kind=$kind, " +
            "cardinality=$cardinality, " +
            "number=$number, " +
            "name=$name, " +
            "typeUrl=$typeUrl, " +
            "oneofIndex=$oneofIndex, " +
            "packed=$packed, " +
            "options=$options, " +
            "jsonName=$jsonName, " +
            "defaultValue=$defaultValue" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: FieldDsl.() -> Unit): Field = Field.Deserializer {
        kind = this@Field.kind
        cardinality = this@Field.cardinality
        number = this@Field.number
        name = this@Field.name
        typeUrl = this@Field.typeUrl
        oneofIndex = this@Field.oneofIndex
        packed = this@Field.packed
        options = this@Field.options
        jsonName = this@Field.jsonName
        defaultValue = this@Field.defaultValue
        unknownFields = this@Field.unknownFields
        dsl()
    }

    class FieldDsl {
        var kind: Kind = Kind.from(0)

        var cardinality: Cardinality = Cardinality.from(0)

        var number: Int = 0

        var name: String = ""

        var typeUrl: String = ""

        var oneofIndex: Int = 0

        var packed: Boolean = false

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var jsonName: String = ""

        var defaultValue: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Field = Field(kind,
            cardinality,
            number,
            name,
            typeUrl,
            oneofIndex,
            packed,
            finishList(options),
            jsonName,
            defaultValue,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Field>, (FieldDsl.() -> Unit) -> Field {
        override fun deserialize(deserializer: KtMessageDeserializer): Field {
            var kind = Kind.from(0)
            var cardinality = Cardinality.from(0)
            var number = 0
            var name = ""
            var typeUrl = ""
            var oneofIndex = 0
            var packed = false
            var options : MutableList<Option>? = null
            var jsonName = ""
            var defaultValue = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Field(kind,
                        cardinality,
                        number,
                        name,
                        typeUrl,
                        oneofIndex,
                        packed,
                        finishList(options),
                        jsonName,
                        defaultValue,
                        UnknownFieldSet.from(unknownFields))
                    8 -> kind = deserializer.readEnum(com.toasttab.protokt.Field.Kind)
                    16 -> cardinality =
                        deserializer.readEnum(com.toasttab.protokt.Field.Cardinality)
                    24 -> number = deserializer.readInt32()
                    34 -> name = deserializer.readString()
                    50 -> typeUrl = deserializer.readString()
                    56 -> oneofIndex = deserializer.readInt32()
                    64 -> packed = deserializer.readBool()
                    74 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    82 -> jsonName = deserializer.readString()
                    90 -> defaultValue = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: FieldDsl.() -> Unit): Field = FieldDsl().apply(dsl).build()
    }

    /**
     * Basic field types.
     */
    sealed class Kind(
        override val `value`: Int,
        override val name: String,
    ) : KtEnum() {
        /**
         * Field type unknown.
         */
        object TYPE_UNKNOWN : Kind(0, "TYPE_UNKNOWN")

        /**
         * Field type double.
         */
        object TYPE_DOUBLE : Kind(1, "TYPE_DOUBLE")

        /**
         * Field type float.
         */
        object TYPE_FLOAT : Kind(2, "TYPE_FLOAT")

        /**
         * Field type int64.
         */
        object TYPE_INT64 : Kind(3, "TYPE_INT64")

        /**
         * Field type uint64.
         */
        object TYPE_UINT64 : Kind(4, "TYPE_UINT64")

        /**
         * Field type int32.
         */
        object TYPE_INT32 : Kind(5, "TYPE_INT32")

        /**
         * Field type fixed64.
         */
        object TYPE_FIXED64 : Kind(6, "TYPE_FIXED64")

        /**
         * Field type fixed32.
         */
        object TYPE_FIXED32 : Kind(7, "TYPE_FIXED32")

        /**
         * Field type bool.
         */
        object TYPE_BOOL : Kind(8, "TYPE_BOOL")

        /**
         * Field type string.
         */
        object TYPE_STRING : Kind(9, "TYPE_STRING")

        /**
         * Field type group. Proto2 syntax only, and deprecated.
         */
        object TYPE_GROUP : Kind(10, "TYPE_GROUP")

        /**
         * Field type message.
         */
        object TYPE_MESSAGE : Kind(11, "TYPE_MESSAGE")

        /**
         * Field type bytes.
         */
        object TYPE_BYTES : Kind(12, "TYPE_BYTES")

        /**
         * Field type uint32.
         */
        object TYPE_UINT32 : Kind(13, "TYPE_UINT32")

        /**
         * Field type enum.
         */
        object TYPE_ENUM : Kind(14, "TYPE_ENUM")

        /**
         * Field type sfixed32.
         */
        object TYPE_SFIXED32 : Kind(15, "TYPE_SFIXED32")

        /**
         * Field type sfixed64.
         */
        object TYPE_SFIXED64 : Kind(16, "TYPE_SFIXED64")

        /**
         * Field type sint32.
         */
        object TYPE_SINT32 : Kind(17, "TYPE_SINT32")

        /**
         * Field type sint64.
         */
        object TYPE_SINT64 : Kind(18, "TYPE_SINT64")

        class UNRECOGNIZED(
            `value`: Int,
        ) : Kind(value, "UNRECOGNIZED")

        companion object Deserializer : KtEnumDeserializer<Kind> {
            override fun from(`value`: Int): Kind = when (value) {
                0 -> TYPE_UNKNOWN
                1 -> TYPE_DOUBLE
                2 -> TYPE_FLOAT
                3 -> TYPE_INT64
                4 -> TYPE_UINT64
                5 -> TYPE_INT32
                6 -> TYPE_FIXED64
                7 -> TYPE_FIXED32
                8 -> TYPE_BOOL
                9 -> TYPE_STRING
                10 -> TYPE_GROUP
                11 -> TYPE_MESSAGE
                12 -> TYPE_BYTES
                13 -> TYPE_UINT32
                14 -> TYPE_ENUM
                15 -> TYPE_SFIXED32
                16 -> TYPE_SFIXED64
                17 -> TYPE_SINT32
                18 -> TYPE_SINT64
                else -> UNRECOGNIZED(value)
            }
        }
    }

    /**
     * Whether a field is optional, required, or repeated.
     */
    sealed class Cardinality(
        override val `value`: Int,
        override val name: String,
    ) : KtEnum() {
        /**
         * For fields with unknown cardinality.
         */
        object UNKNOWN : Cardinality(0, "UNKNOWN")

        /**
         * For optional fields.
         */
        object OPTIONAL : Cardinality(1, "OPTIONAL")

        /**
         * For required fields. Proto2 syntax only.
         */
        object REQUIRED : Cardinality(2, "REQUIRED")

        /**
         * For repeated fields.
         */
        object REPEATED : Cardinality(3, "REPEATED")

        class UNRECOGNIZED(
            `value`: Int,
        ) : Cardinality(value, "UNRECOGNIZED")

        companion object Deserializer : KtEnumDeserializer<Cardinality> {
            override fun from(`value`: Int): Cardinality = when (value) {
                0 -> UNKNOWN
                1 -> OPTIONAL
                2 -> REQUIRED
                3 -> REPEATED
                else -> UNRECOGNIZED(value)
            }
        }
    }
}

/**
 * Enum type definition.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Enum")
class Enum_ private constructor(
    /**
     * Enum type name.
     */
    val name: String,
    /**
     * Enum value definitions.
     */
    val enumvalue: List<EnumValue>,
    /**
     * Protocol buffer options.
     */
    val options: List<Option>,
    /**
     * The source context.
     */
    val sourceContext: SourceContext?,
    /**
     * The source syntax.
     */
    val syntax: Syntax,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (enumvalue.isNotEmpty()) {
            result += (sizeof(Tag(2)) * enumvalue.size) + enumvalue.sumOf { sizeof(it) }
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(3)) * options.size) + options.sumOf { sizeof(it) }
        }
        if (sourceContext  != null) {
            result += sizeof(Tag(4)) + sizeof(sourceContext)
        }
        if (syntax.value != 0) {
            result += sizeof(Tag(5)) + sizeof(syntax)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (enumvalue.isNotEmpty()) {
            enumvalue.forEach { serializer.write(Tag(18)).write(it) }
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(26)).write(it) }
        }
        if (sourceContext  != null) {
            serializer.write(Tag(34)).write(sourceContext)
        }
        if (syntax.value != 0) {
            serializer.write(Tag(40)).write(syntax)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: kotlin.Any?): Boolean = other is Enum_ &&
            other.name == name &&
            other.enumvalue == enumvalue &&
            other.options == options &&
            other.sourceContext == sourceContext &&
            other.syntax == syntax &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + enumvalue.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + sourceContext.hashCode()
        result = 31 * result + syntax.hashCode()
        return result
    }

    override fun toString(): String = "Enum_(" +
            "name=$name, " +
            "enumvalue=$enumvalue, " +
            "options=$options, " +
            "sourceContext=$sourceContext, " +
            "syntax=$syntax" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: Enum_Dsl.() -> Unit): Enum_ = Enum_.Deserializer {
        name = this@Enum_.name
        enumvalue = this@Enum_.enumvalue
        options = this@Enum_.options
        sourceContext = this@Enum_.sourceContext
        syntax = this@Enum_.syntax
        unknownFields = this@Enum_.unknownFields
        dsl()
    }

    class Enum_Dsl {
        var name: String = ""

        var enumvalue: List<EnumValue> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var sourceContext: SourceContext? = null

        var syntax: Syntax = Syntax.from(0)

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Enum_ = Enum_(name,
            finishList(enumvalue),
            finishList(options),
            sourceContext,
            syntax,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Enum_>, (Enum_Dsl.() -> Unit) -> Enum_ {
        override fun deserialize(deserializer: KtMessageDeserializer): Enum_ {
            var name = ""
            var enumvalue : MutableList<EnumValue>? = null
            var options : MutableList<Option>? = null
            var sourceContext : SourceContext? = null
            var syntax = Syntax.from(0)
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Enum_(name,
                        finishList(enumvalue),
                        finishList(options),
                        sourceContext,
                        syntax,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> enumvalue = (enumvalue ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.EnumValue))
                        }
                    }
                    26 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    34 -> sourceContext =
                        deserializer.readMessage(com.toasttab.protokt.SourceContext)
                    40 -> syntax = deserializer.readEnum(com.toasttab.protokt.Syntax)
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: Enum_Dsl.() -> Unit): Enum_ = Enum_Dsl().apply(dsl).build()
    }
}

/**
 * Enum value definition.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.EnumValue")
class EnumValue private constructor(
    /**
     * Enum value name.
     */
    val name: String,
    /**
     * Enum value number.
     */
    val number: Int,
    /**
     * Protocol buffer options.
     */
    val options: List<Option>,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (number != 0) {
            result += sizeof(Tag(2)) + sizeof(Int32(number))
        }
        if (options.isNotEmpty()) {
            result += (sizeof(Tag(3)) * options.size) + options.sumOf { sizeof(it) }
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (number != 0) {
            serializer.write(Tag(16)).write(Int32(number))
        }
        if (options.isNotEmpty()) {
            options.forEach { serializer.write(Tag(26)).write(it) }
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: kotlin.Any?): Boolean = other is EnumValue &&
            other.name == name &&
            other.number == number &&
            other.options == options &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    override fun toString(): String = "EnumValue(" +
            "name=$name, " +
            "number=$number, " +
            "options=$options" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: EnumValueDsl.() -> Unit): EnumValue = EnumValue.Deserializer {
        name = this@EnumValue.name
        number = this@EnumValue.number
        options = this@EnumValue.options
        unknownFields = this@EnumValue.unknownFields
        dsl()
    }

    class EnumValueDsl {
        var name: String = ""

        var number: Int = 0

        var options: List<Option> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): EnumValue = EnumValue(name,
            number,
            finishList(options),
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<EnumValue>,
            (EnumValueDsl.() -> Unit) -> EnumValue {
        override fun deserialize(deserializer: KtMessageDeserializer): EnumValue {
            var name = ""
            var number = 0
            var options : MutableList<Option>? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return EnumValue(name,
                        number,
                        finishList(options),
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    16 -> number = deserializer.readInt32()
                    26 -> options = (options ?: mutableListOf()).apply {
                        deserializer.readRepeated(false) {
                            add(deserializer.readMessage(com.toasttab.protokt.Option))
                        }
                    }
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: EnumValueDsl.() -> Unit): EnumValue =
            EnumValueDsl().apply(dsl).build()
    }
}

/**
 * A protocol buffer option, which can be attached to a message, field, enumeration, etc.
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Option")
class Option private constructor(
    /**
     * The option's name. For protobuf built-in options (options defined in descriptor.proto), this
     * is the short name. For example, `"map_entry"`. For custom options, it should be the
     * fully-qualified name. For example, `"google.api.http"`.
     */
    val name: String,
    /**
     * The option's value packed in an Any message. If the value is a primitive, the corresponding
     * wrapper type defined in google/protobuf/wrappers.proto should be used. If the value is an enum,
     * it should be stored as an int32 value using the google.protobuf.Int32Value type.
     */
    val `value`: Any?,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (value  != null) {
            result += sizeof(Tag(2)) + sizeof(value)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (value  != null) {
            serializer.write(Tag(18)).write(value)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: kotlin.Any?): Boolean = other is Option &&
            other.name == name &&
            other.value == value &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = "Option(" +
            "name=$name, " +
            "value=$value" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: OptionDsl.() -> Unit): Option = Option.Deserializer {
        name = this@Option.name
        value = this@Option.value
        unknownFields = this@Option.unknownFields
        dsl()
    }

    class OptionDsl {
        var name: String = ""

        var `value`: Any? = null

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Option = Option(name,
            value,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Option>, (OptionDsl.() -> Unit) -> Option
    {
        override fun deserialize(deserializer: KtMessageDeserializer): Option {
            var name = ""
            var value : Any? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Option(name,
                        value,
                        UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> value = deserializer.readMessage(com.toasttab.protokt.Any)
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: OptionDsl.() -> Unit): Option =
            OptionDsl().apply(dsl).build()
    }
}
