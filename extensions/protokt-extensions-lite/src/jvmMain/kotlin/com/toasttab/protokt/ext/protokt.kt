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

// Generated by protokt version 0.11.6. Do not modify.
// Source: protokt/protokt.proto
package com.toasttab.protokt.ext

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import protokt.v1.AbstractKtMessage
import protokt.v1.NewToOldAdapter
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

@KtGeneratedMessage("protokt.ProtoktFileOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktFileOptions")
@Deprecated("use v1")
class ProtoktFileOptions private constructor(
    /**
     * Specify the Kotlin package for the generated file. Precedence is given first to this Kotlin
     * package, then to the Java package if enabled in the plugin options, and finally to the protobuf
     * package.
     */
    val kotlinPackage: String,
    /**
     * Specify the name of the Kotlin object that contains the reference to this file's
     * FileDescriptor object.
     */
    val fileDescriptorObjectName: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (kotlinPackage.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(kotlinPackage)
        }
        if (fileDescriptorObjectName.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(fileDescriptorObjectName)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (kotlinPackage.isNotEmpty()) {
            adapter.write(Tag(10)).write(kotlinPackage)
        }
        if (fileDescriptorObjectName.isNotEmpty()) {
            adapter.write(Tag(18)).write(fileDescriptorObjectName)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktFileOptions &&
        other.kotlinPackage == kotlinPackage &&
        other.fileDescriptorObjectName == fileDescriptorObjectName &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + kotlinPackage.hashCode()
        result = 31 * result + fileDescriptorObjectName.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktFileOptions(" +
        "kotlinPackage=$kotlinPackage, " +
        "fileDescriptorObjectName=$fileDescriptorObjectName" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktFileOptionsDsl.() -> Unit): ProtoktFileOptions =
            ProtoktFileOptions.Deserializer {
        kotlinPackage = this@ProtoktFileOptions.kotlinPackage
        fileDescriptorObjectName = this@ProtoktFileOptions.fileDescriptorObjectName
        unknownFields = this@ProtoktFileOptions.unknownFields
        dsl()
    }

    class ProtoktFileOptionsDsl {
        var kotlinPackage: String = ""

        var fileDescriptorObjectName: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktFileOptions = ProtoktFileOptions(kotlinPackage,
        fileDescriptorObjectName,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktFileOptions>,
            (ProtoktFileOptionsDsl.() -> Unit) -> ProtoktFileOptions {
        override fun deserialize(deserializer: KtMessageDeserializer): ProtoktFileOptions {
            var kotlinPackage = ""
            var fileDescriptorObjectName = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktFileOptions(kotlinPackage,
                            fileDescriptorObjectName,
                            UnknownFieldSet.from(unknownFields))
                    10 -> kotlinPackage = deserializer.readString()
                    18 -> fileDescriptorObjectName = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktFileOptionsDsl.() -> Unit): ProtoktFileOptions =
                ProtoktFileOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktMessageOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktMessageOptions")
@Deprecated("use v1")
class ProtoktMessageOptions private constructor(
    /**
     * Declares that the message class implements an interface. Scoping rules are the same as those
     * for declaring wrapper types.
     */
    val implements: String,
    /**
     * Provides a message for deprecation
     */
    val deprecationMessage: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (implements.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(implements)
        }
        if (deprecationMessage.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(deprecationMessage)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (implements.isNotEmpty()) {
            adapter.write(Tag(10)).write(implements)
        }
        if (deprecationMessage.isNotEmpty()) {
            adapter.write(Tag(18)).write(deprecationMessage)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktMessageOptions &&
        other.implements == implements &&
        other.deprecationMessage == deprecationMessage &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + implements.hashCode()
        result = 31 * result + deprecationMessage.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktMessageOptions(" +
        "implements=$implements, " +
        "deprecationMessage=$deprecationMessage" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktMessageOptionsDsl.() -> Unit): ProtoktMessageOptions =
            ProtoktMessageOptions.Deserializer {
        implements = this@ProtoktMessageOptions.implements
        deprecationMessage = this@ProtoktMessageOptions.deprecationMessage
        unknownFields = this@ProtoktMessageOptions.unknownFields
        dsl()
    }

    class ProtoktMessageOptionsDsl {
        var implements: String = ""

        var deprecationMessage: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktMessageOptions = ProtoktMessageOptions(implements,
        deprecationMessage,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktMessageOptions>,
            (ProtoktMessageOptionsDsl.() -> Unit) -> ProtoktMessageOptions {
        override fun deserialize(deserializer: KtMessageDeserializer):
                ProtoktMessageOptions {
            var implements = ""
            var deprecationMessage = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktMessageOptions(implements,
                            deprecationMessage,
                            UnknownFieldSet.from(unknownFields))
                    10 -> implements = deserializer.readString()
                    18 -> deprecationMessage = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktMessageOptionsDsl.() -> Unit): ProtoktMessageOptions
                = ProtoktMessageOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktFieldOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktFieldOptions")
@Deprecated("use v1")
class ProtoktFieldOptions private constructor(
    /**
     * Makes a message-type field non-nullable in the generated Kotlin code. Beware that
     * deserialization will NPE if the field is missing from the protobuf payload. Adding a non-null
     * field to an existing message is a backwards-incompatible change.
     *
     *  For example:
     *
     *  message Foo {   string id = 1 [(protokt.property).non_null = true]; }
     */
    val nonNull: Boolean,
    /**
     * Expose a wrapper class instead of a raw protobuf type.
     *
     *  For example:
     *
     *  message Foo {   string id = 1 [(protokt.property).wrap = "com.foo.FooId"]; }
     *
     *  data class FooId(val value: String)
     *
     *  will yield: class Foo(val id: FooId) ...
     *
     *  If the Kotlin package (or Java package, if the Kotlin package is unspecified) of this file
     * is the same as the package of the wrapper type, full qualification is optional.
     *
     *  This option can be applied to repeated fields.
     */
    val wrap: String,
    /**
     * Maps a bytes field to BytesSlice. If deserialized from a byte array, BytesSlice will point to
     * the source array without copying the subarray.
     */
    val bytesSlice: Boolean,
    /**
     * Provides a message for deprecation
     */
    val deprecationMessage: String,
    /**
     * Expose a wrapper class instead of a raw protobuf type for the key type of a map.
     *
     *  For example:
     *
     *  message Foo {   map<string, int32> map = 1 [(protokt.property).key_wrap = "com.foo.FooId"];
     * }
     *
     *  data class FooId(val value: String)
     *
     *  will yield: class Foo(val map: Map<FooId, String>) ...
     *
     *  Scoping rules  are the same as those for declaring regular field wrapper types.
     */
    val keyWrap: String,
    /**
     * Expose a wrapper class instead of a raw protobuf type for the value type of a map.
     *
     *  For example:
     *
     *  message Foo {   map<int32, strig> map = 1 [(protokt.property).value_wrap = "com.foo.FooId"];
     * }
     *
     *  data class FooId(val value: String)
     *
     *  will yield: class Foo(val map: Map<Int, FooId>) ...
     *
     *  Scoping rules  are the same as those for declaring regular field wrapper types.
     */
    val valueWrap: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (nonNull) {
            result += sizeof(Tag(1)) + sizeof(nonNull)
        }
        if (wrap.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(wrap)
        }
        if (bytesSlice) {
            result += sizeof(Tag(3)) + sizeof(bytesSlice)
        }
        if (deprecationMessage.isNotEmpty()) {
            result += sizeof(Tag(4)) + sizeof(deprecationMessage)
        }
        if (keyWrap.isNotEmpty()) {
            result += sizeof(Tag(5)) + sizeof(keyWrap)
        }
        if (valueWrap.isNotEmpty()) {
            result += sizeof(Tag(6)) + sizeof(valueWrap)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (nonNull) {
            adapter.write(Tag(8)).write(nonNull)
        }
        if (wrap.isNotEmpty()) {
            adapter.write(Tag(18)).write(wrap)
        }
        if (bytesSlice) {
            adapter.write(Tag(24)).write(bytesSlice)
        }
        if (deprecationMessage.isNotEmpty()) {
            adapter.write(Tag(34)).write(deprecationMessage)
        }
        if (keyWrap.isNotEmpty()) {
            adapter.write(Tag(42)).write(keyWrap)
        }
        if (valueWrap.isNotEmpty()) {
            adapter.write(Tag(50)).write(valueWrap)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktFieldOptions &&
        other.nonNull == nonNull &&
        other.wrap == wrap &&
        other.bytesSlice == bytesSlice &&
        other.deprecationMessage == deprecationMessage &&
        other.keyWrap == keyWrap &&
        other.valueWrap == valueWrap &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + nonNull.hashCode()
        result = 31 * result + wrap.hashCode()
        result = 31 * result + bytesSlice.hashCode()
        result = 31 * result + deprecationMessage.hashCode()
        result = 31 * result + keyWrap.hashCode()
        result = 31 * result + valueWrap.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktFieldOptions(" +
        "nonNull=$nonNull, " +
        "wrap=$wrap, " +
        "bytesSlice=$bytesSlice, " +
        "deprecationMessage=$deprecationMessage, " +
        "keyWrap=$keyWrap, " +
        "valueWrap=$valueWrap" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktFieldOptionsDsl.() -> Unit): ProtoktFieldOptions =
            ProtoktFieldOptions.Deserializer {
        nonNull = this@ProtoktFieldOptions.nonNull
        wrap = this@ProtoktFieldOptions.wrap
        bytesSlice = this@ProtoktFieldOptions.bytesSlice
        deprecationMessage = this@ProtoktFieldOptions.deprecationMessage
        keyWrap = this@ProtoktFieldOptions.keyWrap
        valueWrap = this@ProtoktFieldOptions.valueWrap
        unknownFields = this@ProtoktFieldOptions.unknownFields
        dsl()
    }

    class ProtoktFieldOptionsDsl {
        var nonNull: Boolean = false

        var wrap: String = ""

        var bytesSlice: Boolean = false

        var deprecationMessage: String = ""

        var keyWrap: String = ""

        var valueWrap: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktFieldOptions = ProtoktFieldOptions(nonNull,
        wrap,
        bytesSlice,
        deprecationMessage,
        keyWrap,
        valueWrap,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktFieldOptions>,
            (ProtoktFieldOptionsDsl.() -> Unit) -> ProtoktFieldOptions {
        override fun deserialize(deserializer: KtMessageDeserializer): ProtoktFieldOptions {
            var nonNull = false
            var wrap = ""
            var bytesSlice = false
            var deprecationMessage = ""
            var keyWrap = ""
            var valueWrap = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktFieldOptions(nonNull,
                            wrap,
                            bytesSlice,
                            deprecationMessage,
                            keyWrap,
                            valueWrap,
                            UnknownFieldSet.from(unknownFields))
                    8 -> nonNull = deserializer.readBool()
                    18 -> wrap = deserializer.readString()
                    24 -> bytesSlice = deserializer.readBool()
                    34 -> deprecationMessage = deserializer.readString()
                    42 -> keyWrap = deserializer.readString()
                    50 -> valueWrap = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktFieldOptionsDsl.() -> Unit): ProtoktFieldOptions =
                ProtoktFieldOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktOneofOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktOneofOptions")
@Deprecated("use v1")
class ProtoktOneofOptions private constructor(
    /**
     * Makes a oneof field non-nullable in generated Kotlin code. Beware that deserialization will
     * NPE if the field is missing from the protobuf payload. Adding a non-null field to an existing
     * message is a backwards-incompatible change.
     *
     *  For example:
     *
     *  message Message {   oneof some_field_name {     option (protokt.oneof).non_null = true;
     *
     *      string id = 1;   } }
     */
    val nonNull: Boolean,
    /**
     * Make the sealed class implement an interface, enforcing the presence of a property in each
     * possible variant. Scoping rules  are the same as those for declaring wrapper types.
     */
    val implements: String,
    /**
     * Provides a message for deprecation
     */
    val deprecationMessage: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (nonNull) {
            result += sizeof(Tag(1)) + sizeof(nonNull)
        }
        if (implements.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(implements)
        }
        if (deprecationMessage.isNotEmpty()) {
            result += sizeof(Tag(3)) + sizeof(deprecationMessage)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (nonNull) {
            adapter.write(Tag(8)).write(nonNull)
        }
        if (implements.isNotEmpty()) {
            adapter.write(Tag(18)).write(implements)
        }
        if (deprecationMessage.isNotEmpty()) {
            adapter.write(Tag(26)).write(deprecationMessage)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktOneofOptions &&
        other.nonNull == nonNull &&
        other.implements == implements &&
        other.deprecationMessage == deprecationMessage &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + nonNull.hashCode()
        result = 31 * result + implements.hashCode()
        result = 31 * result + deprecationMessage.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktOneofOptions(" +
        "nonNull=$nonNull, " +
        "implements=$implements, " +
        "deprecationMessage=$deprecationMessage" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktOneofOptionsDsl.() -> Unit): ProtoktOneofOptions =
            ProtoktOneofOptions.Deserializer {
        nonNull = this@ProtoktOneofOptions.nonNull
        implements = this@ProtoktOneofOptions.implements
        deprecationMessage = this@ProtoktOneofOptions.deprecationMessage
        unknownFields = this@ProtoktOneofOptions.unknownFields
        dsl()
    }

    class ProtoktOneofOptionsDsl {
        var nonNull: Boolean = false

        var implements: String = ""

        var deprecationMessage: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktOneofOptions = ProtoktOneofOptions(nonNull,
        implements,
        deprecationMessage,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktOneofOptions>,
            (ProtoktOneofOptionsDsl.() -> Unit) -> ProtoktOneofOptions {
        override fun deserialize(deserializer: KtMessageDeserializer): ProtoktOneofOptions {
            var nonNull = false
            var implements = ""
            var deprecationMessage = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktOneofOptions(nonNull,
                            implements,
                            deprecationMessage,
                            UnknownFieldSet.from(unknownFields))
                    8 -> nonNull = deserializer.readBool()
                    18 -> implements = deserializer.readString()
                    26 -> deprecationMessage = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktOneofOptionsDsl.() -> Unit): ProtoktOneofOptions =
                ProtoktOneofOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktEnumOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktEnumOptions")
@Deprecated("use v1")
class ProtoktEnumOptions private constructor(
    /**
     * Provides a message for deprecation
     */
    val deprecationMessage: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (deprecationMessage.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(deprecationMessage)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (deprecationMessage.isNotEmpty()) {
            adapter.write(Tag(10)).write(deprecationMessage)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktEnumOptions &&
        other.deprecationMessage == deprecationMessage &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + deprecationMessage.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktEnumOptions(" +
        "deprecationMessage=$deprecationMessage" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktEnumOptionsDsl.() -> Unit): ProtoktEnumOptions =
            ProtoktEnumOptions.Deserializer {
        deprecationMessage = this@ProtoktEnumOptions.deprecationMessage
        unknownFields = this@ProtoktEnumOptions.unknownFields
        dsl()
    }

    class ProtoktEnumOptionsDsl {
        var deprecationMessage: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktEnumOptions = ProtoktEnumOptions(deprecationMessage,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktEnumOptions>,
            (ProtoktEnumOptionsDsl.() -> Unit) -> ProtoktEnumOptions {
        override fun deserialize(deserializer: KtMessageDeserializer): ProtoktEnumOptions {
            var deprecationMessage = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktEnumOptions(deprecationMessage,
                            UnknownFieldSet.from(unknownFields))
                    10 -> deprecationMessage = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktEnumOptionsDsl.() -> Unit): ProtoktEnumOptions =
                ProtoktEnumOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktEnumValueOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktEnumValueOptions")
@Deprecated("use v1")
class ProtoktEnumValueOptions private constructor(
    /**
     * Provides a message for deprecation
     */
    val deprecationMessage: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (deprecationMessage.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(deprecationMessage)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (deprecationMessage.isNotEmpty()) {
            adapter.write(Tag(10)).write(deprecationMessage)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktEnumValueOptions &&
        other.deprecationMessage == deprecationMessage &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + deprecationMessage.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktEnumValueOptions(" +
        "deprecationMessage=$deprecationMessage" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktEnumValueOptionsDsl.() -> Unit): ProtoktEnumValueOptions =
            ProtoktEnumValueOptions.Deserializer {
        deprecationMessage = this@ProtoktEnumValueOptions.deprecationMessage
        unknownFields = this@ProtoktEnumValueOptions.unknownFields
        dsl()
    }

    class ProtoktEnumValueOptionsDsl {
        var deprecationMessage: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktEnumValueOptions = ProtoktEnumValueOptions(deprecationMessage,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktEnumValueOptions>,
            (ProtoktEnumValueOptionsDsl.() -> Unit) -> ProtoktEnumValueOptions {
        override fun deserialize(deserializer: KtMessageDeserializer):
                ProtoktEnumValueOptions {
            var deprecationMessage = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktEnumValueOptions(deprecationMessage,
                            UnknownFieldSet.from(unknownFields))
                    10 -> deprecationMessage = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktEnumValueOptionsDsl.() -> Unit):
                ProtoktEnumValueOptions = ProtoktEnumValueOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktServiceOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktServiceOptions")
@Deprecated("use v1")
class ProtoktServiceOptions private constructor(
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int = unknownFields.size()

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
            other is ProtoktServiceOptions && other.unknownFields == unknownFields

    override fun hashCode(): Int = unknownFields.hashCode()

    override fun toString(): String = "ProtoktServiceOptions(${if (unknownFields.isEmpty())
            "" else "unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktServiceOptionsDsl.() -> Unit): ProtoktServiceOptions =
            ProtoktServiceOptions.Deserializer {
        unknownFields = this@ProtoktServiceOptions.unknownFields
        dsl()
    }

    class ProtoktServiceOptionsDsl {
        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktServiceOptions = ProtoktServiceOptions(unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktServiceOptions>,
            (ProtoktServiceOptionsDsl.() -> Unit) -> ProtoktServiceOptions {
        override fun deserialize(deserializer: KtMessageDeserializer):
                ProtoktServiceOptions {
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktServiceOptions(UnknownFieldSet.from(unknownFields))
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktServiceOptionsDsl.() -> Unit): ProtoktServiceOptions
                = ProtoktServiceOptionsDsl().apply(dsl).build()
    }
}

@KtGeneratedMessage("protokt.ProtoktMethodOptions")
@protokt.v1.KtGeneratedMessage("protokt.ProtoktMethodOptions")
@Deprecated("use v1")
class ProtoktMethodOptions private constructor(
    /**
     * Provides a custom request marshaller for the generated method descriptor. Substitutes the
     * provided expression directly for `com.toasttab.protokt.grpc.KtMarshaller(<request_type>)`
     */
    val requestMarshaller: String,
    /**
     * Provides a custom response marshaller for the generated method descriptor. Substitutes the
     * provided expression directly for `com.toasttab.protokt.grpc.KtMarshaller(<response_type>)`
     */
    val responseMarshaller: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : AbstractKtMessage() {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (requestMarshaller.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(requestMarshaller)
        }
        if (responseMarshaller.isNotEmpty()) {
            result += sizeof(Tag(2)) + sizeof(responseMarshaller)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: protokt.v1.KtMessageSerializer) {
        val adapter = NewToOldAdapter(serializer)
        if (requestMarshaller.isNotEmpty()) {
            adapter.write(Tag(10)).write(requestMarshaller)
        }
        if (responseMarshaller.isNotEmpty()) {
            adapter.write(Tag(18)).write(responseMarshaller)
        }
        adapter.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is ProtoktMethodOptions &&
        other.requestMarshaller == requestMarshaller &&
        other.responseMarshaller == responseMarshaller &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + requestMarshaller.hashCode()
        result = 31 * result + responseMarshaller.hashCode()
        return result
    }

    override fun toString(): String = "ProtoktMethodOptions(" +
        "requestMarshaller=$requestMarshaller, " +
        "responseMarshaller=$responseMarshaller" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ProtoktMethodOptionsDsl.() -> Unit): ProtoktMethodOptions =
            ProtoktMethodOptions.Deserializer {
        requestMarshaller = this@ProtoktMethodOptions.requestMarshaller
        responseMarshaller = this@ProtoktMethodOptions.responseMarshaller
        unknownFields = this@ProtoktMethodOptions.unknownFields
        dsl()
    }

    class ProtoktMethodOptionsDsl {
        var requestMarshaller: String = ""

        var responseMarshaller: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): ProtoktMethodOptions = ProtoktMethodOptions(requestMarshaller,
        responseMarshaller,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<ProtoktMethodOptions>,
            (ProtoktMethodOptionsDsl.() -> Unit) -> ProtoktMethodOptions {
        override fun deserialize(deserializer: KtMessageDeserializer): ProtoktMethodOptions {
            var requestMarshaller = ""
            var responseMarshaller = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return ProtoktMethodOptions(requestMarshaller,
                            responseMarshaller,
                            UnknownFieldSet.from(unknownFields))
                    10 -> requestMarshaller = deserializer.readString()
                    18 -> responseMarshaller = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ProtoktMethodOptionsDsl.() -> Unit): ProtoktMethodOptions =
                ProtoktMethodOptionsDsl().apply(dsl).build()
    }
}
