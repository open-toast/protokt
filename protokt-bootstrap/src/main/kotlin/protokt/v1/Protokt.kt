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

@file:Suppress("DEPRECATION")
@file:OptIn(protokt.v1.OnlyForUseByGeneratedProtoCode::class)

package protokt.v1

import protokt.v1.Sizes.sizeOf
import protokt.v1.`get`
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.jvm.JvmStatic

@GeneratedMessage("protokt.v1.FileOptions")
public class FileOptions private constructor(
  private val _fileDescriptorObjectName: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_fileDescriptorObjectName.wireValue().isNotEmpty()) {
      result += sizeOf(10u) + sizeOf(_fileDescriptorObjectName.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Specify the name of the Kotlin object that contains the reference to this file's FileDescriptor object.
   */
  @GeneratedProperty(1)
  public val fileDescriptorObjectName: String
    get() = _fileDescriptorObjectName.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_fileDescriptorObjectName.wireValue().isNotEmpty()) {
      writer.writeTag(10u).write(_fileDescriptorObjectName.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FileOptions &&
      other.fileDescriptorObjectName == this.fileDescriptorObjectName &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + fileDescriptorObjectName.hashCode()
    return result
  }

  override fun toString(): String =
    "FileOptions(" +
      "fileDescriptorObjectName=$fileDescriptorObjectName" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FileOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _fileDescriptorObjectNameRef: LazyReference<Bytes, String>? = null

    public var fileDescriptorObjectName: String
      get() = _fileDescriptorObjectNameRef?.value() ?: ""
      set(newValue) {
        _fileDescriptorObjectNameRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FileOptions =
      FileOptions(
        _fileDescriptorObjectNameRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FileOptions): Builder =
        Builder().also {
          it._fileDescriptorObjectNameRef = msg._fileDescriptorObjectName
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FileOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FileOptions {
      var fileDescriptorObjectName: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FileOptions(
              LazyReference(fileDescriptorObjectName ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            fileDescriptorObjectName = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): FileOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.MessageOptions")
public class MessageOptions private constructor(
  private val _implements: LazyReference<Bytes, String>,
  private val _deprecationMessage: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_implements.wireValue().isNotEmpty()) {
      result += sizeOf(10u) + sizeOf(_implements.wireValue())
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      result += sizeOf(18u) + sizeOf(_deprecationMessage.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Declares that the message class implements an interface. Scoping rules are the same as those for declaring wrapper types.
   */
  @GeneratedProperty(1)
  public val implements: String
    get() = _implements.value()

  /**
   * Provides a message for deprecation
   */
  @GeneratedProperty(2)
  public val deprecationMessage: String
    get() = _deprecationMessage.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_implements.wireValue().isNotEmpty()) {
      writer.writeTag(10u).write(_implements.wireValue())
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      writer.writeTag(18u).write(_deprecationMessage.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is MessageOptions &&
      other.implements == this.implements &&
      other.deprecationMessage == this.deprecationMessage &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + implements.hashCode()
    result = 31 * result + deprecationMessage.hashCode()
    return result
  }

  override fun toString(): String =
    "MessageOptions(" +
      "implements=$implements, " +
      "deprecationMessage=$deprecationMessage" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): MessageOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _implementsRef: LazyReference<Bytes, String>? = null

    public var implements: String
      get() = _implementsRef?.value() ?: ""
      set(newValue) {
        _implementsRef = LazyReference(newValue, StringConverter)
      }

    private var _deprecationMessageRef: LazyReference<Bytes, String>? = null

    public var deprecationMessage: String
      get() = _deprecationMessageRef?.value() ?: ""
      set(newValue) {
        _deprecationMessageRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): MessageOptions =
      MessageOptions(
        _implementsRef ?: LazyReference(Bytes.empty(), StringConverter),
        _deprecationMessageRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: MessageOptions): Builder =
        Builder().also {
          it._implementsRef = msg._implements
          it._deprecationMessageRef = msg._deprecationMessage
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<MessageOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): MessageOptions {
      var implements: Bytes? = null
      var deprecationMessage: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return MessageOptions(
              LazyReference(implements ?: Bytes.empty(), StringConverter),
              LazyReference(deprecationMessage ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            implements = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            deprecationMessage = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): MessageOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.FieldOptions")
public class FieldOptions private constructor(
  /**
   * Generates a non-nullable accessor.
   *
   *  For example:
   *
   *  message Foo {   Bar id = 1 [(protokt.v1.property).generate_non_null_accessor = true]; }
   *
   *  will generate an accessor called [requireId] that has the non-nullable type [Bar] (as opposed to [Bar?]).
   */
  @GeneratedProperty(1)
  public val generateNonNullAccessor: Boolean,
  private val _wrap: LazyReference<Bytes, String>,
  /**
   * Maps a bytes field to BytesSlice. If deserialized from a byte array, BytesSlice will point to the source array without copying the subarray.
   */
  @GeneratedProperty(3)
  public val bytesSlice: Boolean,
  private val _deprecationMessage: LazyReference<Bytes, String>,
  private val _keyWrap: LazyReference<Bytes, String>,
  private val _valueWrap: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (generateNonNullAccessor) {
      result += sizeOf(8u) + 1
    }
    if (_wrap.wireValue().isNotEmpty()) {
      result += sizeOf(18u) + sizeOf(_wrap.wireValue())
    }
    if (bytesSlice) {
      result += sizeOf(24u) + 1
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      result += sizeOf(34u) + sizeOf(_deprecationMessage.wireValue())
    }
    if (_keyWrap.wireValue().isNotEmpty()) {
      result += sizeOf(42u) + sizeOf(_keyWrap.wireValue())
    }
    if (_valueWrap.wireValue().isNotEmpty()) {
      result += sizeOf(50u) + sizeOf(_valueWrap.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Expose a wrapper class instead of a raw protobuf type.
   *
   *  For example:
   *
   *  message Foo {   string id = 1 [(protokt.v1.property).wrap = "com.foo.FooId"]; }
   *
   *  data class FooId(val value: String)
   *
   *  will yield: class Foo(val id: FooId) ...
   *
   *  If the Kotlin package (or Java package, if the Kotlin package is unspecified) of this file is the same as the package of the wrapper type, full qualification is optional.
   *
   *  This option can be applied to repeated fields.
   */
  @GeneratedProperty(2)
  public val wrap: String
    get() = _wrap.value()

  /**
   * Provides a message for deprecation
   */
  @GeneratedProperty(4)
  public val deprecationMessage: String
    get() = _deprecationMessage.value()

  /**
   * Expose a wrapper class instead of a raw protobuf type for the key type of a map.
   *
   *  For example:
   *
   *  message Foo {   map<string, int32> map = 1 [(protokt.v1.property).key_wrap = "com.foo.FooId"]; }
   *
   *  data class FooId(val value: String)
   *
   *  will yield: class Foo(val map: Map<FooId, String>) ...
   *
   *  Scoping rules  are the same as those for declaring regular field wrapper types.
   */
  @GeneratedProperty(5)
  public val keyWrap: String
    get() = _keyWrap.value()

  /**
   * Expose a wrapper class instead of a raw protobuf type for the value type of a map.
   *
   *  For example:
   *
   *  message Foo {   map<int32, string> map = 1 [(protokt.v1.property).value_wrap = "com.foo.FooId"]; }
   *
   *  data class FooId(val value: String)
   *
   *  will yield: class Foo(val map: Map<Int, FooId>) ...
   *
   *  Scoping rules  are the same as those for declaring regular field wrapper types.
   */
  @GeneratedProperty(6)
  public val valueWrap: String
    get() = _valueWrap.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (generateNonNullAccessor) {
      writer.writeTag(8u).write(generateNonNullAccessor)
    }
    if (_wrap.wireValue().isNotEmpty()) {
      writer.writeTag(18u).write(_wrap.wireValue())
    }
    if (bytesSlice) {
      writer.writeTag(24u).write(bytesSlice)
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      writer.writeTag(34u).write(_deprecationMessage.wireValue())
    }
    if (_keyWrap.wireValue().isNotEmpty()) {
      writer.writeTag(42u).write(_keyWrap.wireValue())
    }
    if (_valueWrap.wireValue().isNotEmpty()) {
      writer.writeTag(50u).write(_valueWrap.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FieldOptions &&
      other.generateNonNullAccessor == this.generateNonNullAccessor &&
      other.wrap == this.wrap &&
      other.bytesSlice == this.bytesSlice &&
      other.deprecationMessage == this.deprecationMessage &&
      other.keyWrap == this.keyWrap &&
      other.valueWrap == this.valueWrap &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + generateNonNullAccessor.hashCode()
    result = 31 * result + wrap.hashCode()
    result = 31 * result + bytesSlice.hashCode()
    result = 31 * result + deprecationMessage.hashCode()
    result = 31 * result + keyWrap.hashCode()
    result = 31 * result + valueWrap.hashCode()
    return result
  }

  override fun toString(): String =
    "FieldOptions(" +
      "generateNonNullAccessor=$generateNonNullAccessor, " +
      "wrap=$wrap, " +
      "bytesSlice=$bytesSlice, " +
      "deprecationMessage=$deprecationMessage, " +
      "keyWrap=$keyWrap, " +
      "valueWrap=$valueWrap" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FieldOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var generateNonNullAccessor: Boolean = false

    private var _wrapRef: LazyReference<Bytes, String>? = null

    public var wrap: String
      get() = _wrapRef?.value() ?: ""
      set(newValue) {
        _wrapRef = LazyReference(newValue, StringConverter)
      }

    public var bytesSlice: Boolean = false

    private var _deprecationMessageRef: LazyReference<Bytes, String>? = null

    public var deprecationMessage: String
      get() = _deprecationMessageRef?.value() ?: ""
      set(newValue) {
        _deprecationMessageRef = LazyReference(newValue, StringConverter)
      }

    private var _keyWrapRef: LazyReference<Bytes, String>? = null

    public var keyWrap: String
      get() = _keyWrapRef?.value() ?: ""
      set(newValue) {
        _keyWrapRef = LazyReference(newValue, StringConverter)
      }

    private var _valueWrapRef: LazyReference<Bytes, String>? = null

    public var valueWrap: String
      get() = _valueWrapRef?.value() ?: ""
      set(newValue) {
        _valueWrapRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FieldOptions =
      FieldOptions(
        generateNonNullAccessor,
        _wrapRef ?: LazyReference(Bytes.empty(), StringConverter),
        bytesSlice,
        _deprecationMessageRef ?: LazyReference(Bytes.empty(), StringConverter),
        _keyWrapRef ?: LazyReference(Bytes.empty(), StringConverter),
        _valueWrapRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FieldOptions): Builder =
        Builder().also {
          it.generateNonNullAccessor = msg.generateNonNullAccessor
          it._wrapRef = msg._wrap
          it.bytesSlice = msg.bytesSlice
          it._deprecationMessageRef = msg._deprecationMessage
          it._keyWrapRef = msg._keyWrap
          it._valueWrapRef = msg._valueWrap
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FieldOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FieldOptions {
      var generateNonNullAccessor = false
      var wrap: Bytes? = null
      var bytesSlice = false
      var deprecationMessage: Bytes? = null
      var keyWrap: Bytes? = null
      var valueWrap: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FieldOptions(
              generateNonNullAccessor,
              LazyReference(wrap ?: Bytes.empty(), StringConverter),
              bytesSlice,
              LazyReference(deprecationMessage ?: Bytes.empty(), StringConverter),
              LazyReference(keyWrap ?: Bytes.empty(), StringConverter),
              LazyReference(valueWrap ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            generateNonNullAccessor = reader.readBool()
          }

          18u -> {
            wrap = StringConverter.readValidatedBytes(reader)
          }

          24u -> {
            bytesSlice = reader.readBool()
          }

          34u -> {
            deprecationMessage = StringConverter.readValidatedBytes(reader)
          }

          42u -> {
            keyWrap = StringConverter.readValidatedBytes(reader)
          }

          50u -> {
            valueWrap = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): FieldOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.OneofOptions")
public class OneofOptions private constructor(
  /**
   * Generates a non-nullable accessor for a oneof field.
   *
   *  For example:
   *
   *  message Message {   oneof some_field_name {     option (protokt.v1.oneof).generate_non_null_accessor = true;
   *
   *      string id = 1;   } }
   */
  @GeneratedProperty(1)
  public val generateNonNullAccessor: Boolean,
  private val _implements: LazyReference<Bytes, String>,
  private val _deprecationMessage: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (generateNonNullAccessor) {
      result += sizeOf(8u) + 1
    }
    if (_implements.wireValue().isNotEmpty()) {
      result += sizeOf(18u) + sizeOf(_implements.wireValue())
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      result += sizeOf(26u) + sizeOf(_deprecationMessage.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Make the sealed class implement an interface, enforcing the presence of a property in each possible variant. Scoping rules  are the same as those for declaring wrapper types.
   */
  @GeneratedProperty(2)
  public val implements: String
    get() = _implements.value()

  /**
   * Provides a message for deprecation
   */
  @GeneratedProperty(3)
  public val deprecationMessage: String
    get() = _deprecationMessage.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (generateNonNullAccessor) {
      writer.writeTag(8u).write(generateNonNullAccessor)
    }
    if (_implements.wireValue().isNotEmpty()) {
      writer.writeTag(18u).write(_implements.wireValue())
    }
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      writer.writeTag(26u).write(_deprecationMessage.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is OneofOptions &&
      other.generateNonNullAccessor == this.generateNonNullAccessor &&
      other.implements == this.implements &&
      other.deprecationMessage == this.deprecationMessage &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + generateNonNullAccessor.hashCode()
    result = 31 * result + implements.hashCode()
    result = 31 * result + deprecationMessage.hashCode()
    return result
  }

  override fun toString(): String =
    "OneofOptions(" +
      "generateNonNullAccessor=$generateNonNullAccessor, " +
      "implements=$implements, " +
      "deprecationMessage=$deprecationMessage" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): OneofOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var generateNonNullAccessor: Boolean = false

    private var _implementsRef: LazyReference<Bytes, String>? = null

    public var implements: String
      get() = _implementsRef?.value() ?: ""
      set(newValue) {
        _implementsRef = LazyReference(newValue, StringConverter)
      }

    private var _deprecationMessageRef: LazyReference<Bytes, String>? = null

    public var deprecationMessage: String
      get() = _deprecationMessageRef?.value() ?: ""
      set(newValue) {
        _deprecationMessageRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): OneofOptions =
      OneofOptions(
        generateNonNullAccessor,
        _implementsRef ?: LazyReference(Bytes.empty(), StringConverter),
        _deprecationMessageRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: OneofOptions): Builder =
        Builder().also {
          it.generateNonNullAccessor = msg.generateNonNullAccessor
          it._implementsRef = msg._implements
          it._deprecationMessageRef = msg._deprecationMessage
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<OneofOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): OneofOptions {
      var generateNonNullAccessor = false
      var implements: Bytes? = null
      var deprecationMessage: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return OneofOptions(
              generateNonNullAccessor,
              LazyReference(implements ?: Bytes.empty(), StringConverter),
              LazyReference(deprecationMessage ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            generateNonNullAccessor = reader.readBool()
          }

          18u -> {
            implements = StringConverter.readValidatedBytes(reader)
          }

          26u -> {
            deprecationMessage = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): OneofOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.EnumOptions")
public class EnumOptions private constructor(
  private val _deprecationMessage: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      result += sizeOf(10u) + sizeOf(_deprecationMessage.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Provides a message for deprecation
   */
  @GeneratedProperty(1)
  public val deprecationMessage: String
    get() = _deprecationMessage.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      writer.writeTag(10u).write(_deprecationMessage.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumOptions &&
      other.deprecationMessage == this.deprecationMessage &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + deprecationMessage.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumOptions(" +
      "deprecationMessage=$deprecationMessage" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _deprecationMessageRef: LazyReference<Bytes, String>? = null

    public var deprecationMessage: String
      get() = _deprecationMessageRef?.value() ?: ""
      set(newValue) {
        _deprecationMessageRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumOptions =
      EnumOptions(
        _deprecationMessageRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumOptions): Builder =
        Builder().also {
          it._deprecationMessageRef = msg._deprecationMessage
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumOptions {
      var deprecationMessage: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumOptions(
              LazyReference(deprecationMessage ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            deprecationMessage = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): EnumOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.EnumValueOptions")
public class EnumValueOptions private constructor(
  private val _deprecationMessage: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      result += sizeOf(10u) + sizeOf(_deprecationMessage.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Provides a message for deprecation
   */
  @GeneratedProperty(1)
  public val deprecationMessage: String
    get() = _deprecationMessage.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_deprecationMessage.wireValue().isNotEmpty()) {
      writer.writeTag(10u).write(_deprecationMessage.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumValueOptions &&
      other.deprecationMessage == this.deprecationMessage &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + deprecationMessage.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumValueOptions(" +
      "deprecationMessage=$deprecationMessage" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumValueOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _deprecationMessageRef: LazyReference<Bytes, String>? = null

    public var deprecationMessage: String
      get() = _deprecationMessageRef?.value() ?: ""
      set(newValue) {
        _deprecationMessageRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumValueOptions =
      EnumValueOptions(
        _deprecationMessageRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumValueOptions): Builder =
        Builder().also {
          it._deprecationMessageRef = msg._deprecationMessage
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumValueOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumValueOptions {
      var deprecationMessage: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumValueOptions(
              LazyReference(deprecationMessage ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            deprecationMessage = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): EnumValueOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.ServiceOptions")
public class ServiceOptions private constructor(
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    unknownFields.size()
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is ServiceOptions && other.unknownFields == unknownFields

  override fun hashCode(): Int =
    unknownFields.hashCode()

  override fun toString(): String =
    "ServiceOptions(${if (unknownFields.isEmpty()) ")" else "unknownFields=$unknownFields)"}"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): ServiceOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): ServiceOptions =
      ServiceOptions(unknownFields)

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: ServiceOptions): Builder =
        Builder().also {
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<ServiceOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): ServiceOptions {
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return ServiceOptions(
              UnknownFieldSet.from(unknownFields)
            )
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): ServiceOptions =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("protokt.v1.MethodOptions")
public class MethodOptions private constructor(
  private val _requestMarshaller: LazyReference<Bytes, String>,
  private val _responseMarshaller: LazyReference<Bytes, String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_requestMarshaller.wireValue().isNotEmpty()) {
      result += sizeOf(10u) + sizeOf(_requestMarshaller.wireValue())
    }
    if (_responseMarshaller.wireValue().isNotEmpty()) {
      result += sizeOf(18u) + sizeOf(_responseMarshaller.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * Provides a custom request marshaller for the generated method descriptor. Substitutes the provided expression directly for `com.toasttab.protokt.grpc.KtMarshaller(<request_type>)`
   */
  @GeneratedProperty(1)
  public val requestMarshaller: String
    get() = _requestMarshaller.value()

  /**
   * Provides a custom response marshaller for the generated method descriptor. Substitutes the provided expression directly for `com.toasttab.protokt.grpc.KtMarshaller(<response_type>)`
   */
  @GeneratedProperty(2)
  public val responseMarshaller: String
    get() = _responseMarshaller.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_requestMarshaller.wireValue().isNotEmpty()) {
      writer.writeTag(10u).write(_requestMarshaller.wireValue())
    }
    if (_responseMarshaller.wireValue().isNotEmpty()) {
      writer.writeTag(18u).write(_responseMarshaller.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is MethodOptions &&
      other.requestMarshaller == this.requestMarshaller &&
      other.responseMarshaller == this.responseMarshaller &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + requestMarshaller.hashCode()
    result = 31 * result + responseMarshaller.hashCode()
    return result
  }

  override fun toString(): String =
    "MethodOptions(" +
      "requestMarshaller=$requestMarshaller, " +
      "responseMarshaller=$responseMarshaller" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): MethodOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _requestMarshallerRef: LazyReference<Bytes, String>? = null

    public var requestMarshaller: String
      get() = _requestMarshallerRef?.value() ?: ""
      set(newValue) {
        _requestMarshallerRef = LazyReference(newValue, StringConverter)
      }

    private var _responseMarshallerRef: LazyReference<Bytes, String>? = null

    public var responseMarshaller: String
      get() = _responseMarshallerRef?.value() ?: ""
      set(newValue) {
        _responseMarshallerRef = LazyReference(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): MethodOptions =
      MethodOptions(
        _requestMarshallerRef ?: LazyReference(Bytes.empty(), StringConverter),
        _responseMarshallerRef ?: LazyReference(Bytes.empty(), StringConverter),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: MethodOptions): Builder =
        Builder().also {
          it._requestMarshallerRef = msg._requestMarshaller
          it._responseMarshallerRef = msg._responseMarshaller
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<MethodOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): MethodOptions {
      var requestMarshaller: Bytes? = null
      var responseMarshaller: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return MethodOptions(
              LazyReference(requestMarshaller ?: Bytes.empty(), StringConverter),
              LazyReference(responseMarshaller ?: Bytes.empty(), StringConverter),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            requestMarshaller = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            responseMarshaller = StringConverter.readValidatedBytes(reader)
          }

          else -> {
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
          }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): MethodOptions =
      Builder().apply(dsl).build()
  }
}

private val fileExtension: Extension<protokt.v1.google.protobuf.FileOptions, FileOptions> =
  Extension(1_253u, ExtensionCodecs.message(FileOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.FileOptions.`file`: FileOptions?
  get() = this[fileExtension]

private val classExtension: Extension<protokt.v1.google.protobuf.MessageOptions, MessageOptions> =
  Extension(1_253u, ExtensionCodecs.message(MessageOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.MessageOptions.`class`: MessageOptions?
  get() = this[classExtension]

private val propertyExtension: Extension<protokt.v1.google.protobuf.FieldOptions, FieldOptions> =
  Extension(1_253u, ExtensionCodecs.message(FieldOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.FieldOptions.`property`: FieldOptions?
  get() = this[propertyExtension]

private val oneofExtension: Extension<protokt.v1.google.protobuf.OneofOptions, OneofOptions> =
  Extension(1_253u, ExtensionCodecs.message(OneofOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.OneofOptions.oneof: OneofOptions?
  get() = this[oneofExtension]

private val enumExtension: Extension<protokt.v1.google.protobuf.EnumOptions, EnumOptions> =
  Extension(1_253u, ExtensionCodecs.message(EnumOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.EnumOptions.`enum`: EnumOptions?
  get() = this[enumExtension]

private val enumValueExtension:
  Extension<protokt.v1.google.protobuf.EnumValueOptions, EnumValueOptions> =
  Extension(1_253u, ExtensionCodecs.message(EnumValueOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.EnumValueOptions.enumValue: EnumValueOptions?
  get() = this[enumValueExtension]

private val serviceExtension: Extension<protokt.v1.google.protobuf.ServiceOptions, ServiceOptions> =
  Extension(1_253u, ExtensionCodecs.message(ServiceOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.ServiceOptions.service: ServiceOptions?
  get() = this[serviceExtension]

private val methodExtension: Extension<protokt.v1.google.protobuf.MethodOptions, MethodOptions> =
  Extension(1_253u, ExtensionCodecs.message(MethodOptions))

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public val protokt.v1.google.protobuf.MethodOptions.method: MethodOptions?
  get() = this[methodExtension]
