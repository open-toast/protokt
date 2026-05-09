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

package protokt.v1.google.protobuf

import protokt.v1.AbstractDeserializer
import protokt.v1.AbstractMessage
import protokt.v1.BuilderDsl
import protokt.v1.BuilderScope
import protokt.v1.Bytes
import protokt.v1.Collections.freezeList
import protokt.v1.Collections.listBuilder
import protokt.v1.Enum
import protokt.v1.EnumDeserializer
import protokt.v1.GeneratedMessage
import protokt.v1.GeneratedProperty
import protokt.v1.LazyConvertingList
import protokt.v1.LazyReference
import protokt.v1.ListBuilder
import protokt.v1.OnlyForUseByGeneratedProtoCode
import protokt.v1.Reader
import protokt.v1.Sizes.sizeOf
import protokt.v1.StringConverter
import protokt.v1.UnknownFieldSet
import protokt.v1.Writer
import kotlin.Any
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import kotlin.ULong
import kotlin.Unit
import kotlin.collections.List
import kotlin.jvm.JvmStatic

/**
 * The protocol compiler can output a FileDescriptorSet containing the .proto files it parses.
 */
public sealed class Edition(
  override val `value`: Int,
  override val name: String
) : Enum() {
  public object EDITION_UNKNOWN : Edition(0, "EDITION_UNKNOWN")

  public object EDITION_LEGACY : Edition(900, "EDITION_LEGACY")

  public object EDITION_PROTO2 : Edition(998, "EDITION_PROTO2")

  public object EDITION_PROTO3 : Edition(999, "EDITION_PROTO3")

  public object EDITION_2023 : Edition(1000, "EDITION_2023")

  public object EDITION_2024 : Edition(1001, "EDITION_2024")

  public object EDITION_1_TEST_ONLY : Edition(1, "EDITION_1_TEST_ONLY")

  public object EDITION_2_TEST_ONLY : Edition(2, "EDITION_2_TEST_ONLY")

  public object EDITION_99997_TEST_ONLY : Edition(99997, "EDITION_99997_TEST_ONLY")

  public object EDITION_99998_TEST_ONLY : Edition(99998, "EDITION_99998_TEST_ONLY")

  public object EDITION_99999_TEST_ONLY : Edition(99999, "EDITION_99999_TEST_ONLY")

  public object EDITION_MAX : Edition(2147483647, "EDITION_MAX")

  public class UNRECOGNIZED(
    `value`: Int
  ) : Edition(value, "UNRECOGNIZED")

  public companion object Deserializer : EnumDeserializer<Edition> {
    override fun deserialize(`value`: Int): Edition =
      when (value) {
        0 -> EDITION_UNKNOWN
        900 -> EDITION_LEGACY
        998 -> EDITION_PROTO2
        999 -> EDITION_PROTO3
        1_000 -> EDITION_2023
        1_001 -> EDITION_2024
        1 -> EDITION_1_TEST_ONLY
        2 -> EDITION_2_TEST_ONLY
        99_997 -> EDITION_99997_TEST_ONLY
        99_998 -> EDITION_99998_TEST_ONLY
        99_999 -> EDITION_99999_TEST_ONLY
        2_147_483_647 -> EDITION_MAX
        else -> UNRECOGNIZED(value)
      }
  }
}

/**
 * The protocol compiler can output a FileDescriptorSet containing the .proto files it parses.
 */
@GeneratedMessage("google.protobuf.FileDescriptorSet")
public class FileDescriptorSet private constructor(
  @GeneratedProperty(1)
  public val `file`: List<FileDescriptorProto>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (`file`.isNotEmpty()) {
      result += (sizeOf(10u) * `file`.size) + `file`.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    `file`.forEach { writer.writeTag(10u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FileDescriptorSet &&
      other.`file` == this.`file` &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + `file`.hashCode()
    return result
  }

  override fun toString(): String =
    "FileDescriptorSet(" +
      "`file`=$`file`" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FileDescriptorSet =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var `file`: List<FileDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FileDescriptorSet =
      FileDescriptorSet(
        freezeList(`file`),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FileDescriptorSet): Builder =
        Builder().also {
          it.`file` = msg.`file`
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FileDescriptorSet>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FileDescriptorSet {
      var `file`: ListBuilder<FileDescriptorProto>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FileDescriptorSet(
              `file`?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            `file` =
              (`file` ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FileDescriptorProto))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): FileDescriptorSet =
      Builder().apply(dsl).build()
  }
}

/**
 * Describes a complete .proto file.
 */
@GeneratedMessage("google.protobuf.FileDescriptorProto")
public class FileDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  private val _package: LazyReference<Bytes, String>?,
  /**
   * Names of files imported by this file.
   */
  @GeneratedProperty(3)
  public val dependency: List<String>,
  /**
   * All top-level definitions in this file.
   */
  @GeneratedProperty(4)
  public val messageType: List<DescriptorProto>,
  @GeneratedProperty(5)
  public val enumType: List<EnumDescriptorProto>,
  @GeneratedProperty(6)
  public val service: List<ServiceDescriptorProto>,
  @GeneratedProperty(7)
  public val extension: List<FieldDescriptorProto>,
  @GeneratedProperty(8)
  public val options: FileOptions?,
  /**
   * This field contains optional information about the original source code. You may safely remove this entire field without harming runtime functionality of the descriptors -- the information is needed only by development tools.
   */
  @GeneratedProperty(9)
  public val sourceCodeInfo: SourceCodeInfo?,
  /**
   * Indexes of the public imported files in the dependency list above.
   */
  @GeneratedProperty(10)
  public val publicDependency: List<Int>,
  /**
   * Indexes of the weak imported files in the dependency list. For Google-internal migration only. Do not use.
   */
  @GeneratedProperty(11)
  public val weakDependency: List<Int>,
  private val _syntax: LazyReference<Bytes, String>?,
  /**
   * The edition of the proto file. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(14)
  public val edition: Edition?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (_package != null) {
      result += sizeOf(18u) + sizeOf(_package.wireValue())
    }
    if (dependency.isNotEmpty()) {
      result +=
        @Suppress("UNCHECKED_CAST")
        (dependency as LazyConvertingList<Bytes, Any>).let { list ->
          (sizeOf(26u) * list.size) +
            run {
              var sum = 0
              for (i in list.indices) sum += sizeOf(list.wireGet(i))
              sum
            }
        }
    }
    if (messageType.isNotEmpty()) {
      result += (sizeOf(34u) * messageType.size) + messageType.sumOf { sizeOf(it) }
    }
    if (enumType.isNotEmpty()) {
      result += (sizeOf(42u) * enumType.size) + enumType.sumOf { sizeOf(it) }
    }
    if (service.isNotEmpty()) {
      result += (sizeOf(50u) * service.size) + service.sumOf { sizeOf(it) }
    }
    if (extension.isNotEmpty()) {
      result += (sizeOf(58u) * extension.size) + extension.sumOf { sizeOf(it) }
    }
    if (options != null) {
      result += sizeOf(66u) + sizeOf(options)
    }
    if (sourceCodeInfo != null) {
      result += sizeOf(74u) + sizeOf(sourceCodeInfo)
    }
    if (publicDependency.isNotEmpty()) {
      result += (sizeOf(80u) * publicDependency.size) + publicDependency.sumOf { sizeOf(it) }
    }
    if (weakDependency.isNotEmpty()) {
      result += (sizeOf(88u) * weakDependency.size) + weakDependency.sumOf { sizeOf(it) }
    }
    if (_syntax != null) {
      result += sizeOf(98u) + sizeOf(_syntax.wireValue())
    }
    if (edition != null) {
      result += sizeOf(112u) + sizeOf(edition)
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  @GeneratedProperty(2)
  public val `package`: String?
    get() = _package?.value()

  /**
   * The syntax of the proto file. The supported values are "proto2", "proto3", and "editions".
   *
   *  If `edition` is present, this value must be "editions". WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(12)
  public val syntax: String?
    get() = _syntax?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    if (_package != null) {
      writer.writeTag(18u).write(_package.wireValue())
    }
    if (dependency.isNotEmpty()) {
      @Suppress("UNCHECKED_CAST")
      (dependency as LazyConvertingList<Bytes, Any>).wireForEach { writer.writeTag(26u).write(it) }
    }
    messageType.forEach { writer.writeTag(34u).write(it) }
    enumType.forEach { writer.writeTag(42u).write(it) }
    service.forEach { writer.writeTag(50u).write(it) }
    extension.forEach { writer.writeTag(58u).write(it) }
    if (options != null) {
      writer.writeTag(66u).write(options)
    }
    if (sourceCodeInfo != null) {
      writer.writeTag(74u).write(sourceCodeInfo)
    }
    publicDependency.forEach { writer.writeTag(80u).write(it) }
    weakDependency.forEach { writer.writeTag(88u).write(it) }
    if (_syntax != null) {
      writer.writeTag(98u).write(_syntax.wireValue())
    }
    if (edition != null) {
      writer.writeTag(112u).write(edition)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FileDescriptorProto &&
      other.name == this.name &&
      other.`package` == this.`package` &&
      other.dependency == this.dependency &&
      other.messageType == this.messageType &&
      other.enumType == this.enumType &&
      other.service == this.service &&
      other.extension == this.extension &&
      other.options == this.options &&
      other.sourceCodeInfo == this.sourceCodeInfo &&
      other.publicDependency == this.publicDependency &&
      other.weakDependency == this.weakDependency &&
      other.syntax == this.syntax &&
      other.edition == this.edition &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + `package`.hashCode()
    result = 31 * result + dependency.hashCode()
    result = 31 * result + messageType.hashCode()
    result = 31 * result + enumType.hashCode()
    result = 31 * result + service.hashCode()
    result = 31 * result + extension.hashCode()
    result = 31 * result + options.hashCode()
    result = 31 * result + sourceCodeInfo.hashCode()
    result = 31 * result + publicDependency.hashCode()
    result = 31 * result + weakDependency.hashCode()
    result = 31 * result + syntax.hashCode()
    result = 31 * result + edition.hashCode()
    return result
  }

  override fun toString(): String =
    "FileDescriptorProto(" +
      "name=$name, " +
      "`package`=$`package`, " +
      "dependency=$dependency, " +
      "messageType=$messageType, " +
      "enumType=$enumType, " +
      "service=$service, " +
      "extension=$extension, " +
      "options=$options, " +
      "sourceCodeInfo=$sourceCodeInfo, " +
      "publicDependency=$publicDependency, " +
      "weakDependency=$weakDependency, " +
      "syntax=$syntax, " +
      "edition=$edition" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FileDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _packageRef: LazyReference<Bytes, String>? = null

    public var `package`: String?
      get() = _packageRef?.value()
      set(newValue) {
        _packageRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var dependency: List<String> = emptyList()
      set(newValue) {
        field = if (newValue is LazyConvertingList<*, *>) newValue else LazyConvertingList.fromKotlin(newValue, StringConverter)
      }

    public var messageType: List<DescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var enumType: List<EnumDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var service: List<ServiceDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var extension: List<FieldDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var options: FileOptions? = null

    public var sourceCodeInfo: SourceCodeInfo? = null

    public var publicDependency: List<Int> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var weakDependency: List<Int> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    private var _syntaxRef: LazyReference<Bytes, String>? = null

    public var syntax: String?
      get() = _syntaxRef?.value()
      set(newValue) {
        _syntaxRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var edition: Edition? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FileDescriptorProto =
      FileDescriptorProto(
        _nameRef,
        _packageRef,
        dependency,
        freezeList(messageType),
        freezeList(enumType),
        freezeList(service),
        freezeList(extension),
        options,
        sourceCodeInfo,
        freezeList(publicDependency),
        freezeList(weakDependency),
        _syntaxRef,
        edition,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FileDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it._packageRef = msg._package
          it.dependency = msg.dependency
          it.messageType = msg.messageType
          it.enumType = msg.enumType
          it.service = msg.service
          it.extension = msg.extension
          it.options = msg.options
          it.sourceCodeInfo = msg.sourceCodeInfo
          it.publicDependency = msg.publicDependency
          it.weakDependency = msg.weakDependency
          it._syntaxRef = msg._syntax
          it.edition = msg.edition
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FileDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FileDescriptorProto {
      var name: Bytes? = null
      var `package`: Bytes? = null
      var dependency: ListBuilder<Any?>? = null
      var messageType: ListBuilder<DescriptorProto>? = null
      var enumType: ListBuilder<EnumDescriptorProto>? = null
      var service: ListBuilder<ServiceDescriptorProto>? = null
      var extension: ListBuilder<FieldDescriptorProto>? = null
      var options: FileOptions? = null
      var sourceCodeInfo: SourceCodeInfo? = null
      var publicDependency: ListBuilder<Int>? = null
      var weakDependency: ListBuilder<Int>? = null
      var syntax: Bytes? = null
      var edition: Edition? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FileDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              `package`?.let { LazyReference(it, StringConverter) },
              dependency?.build()?.let { LazyConvertingList<Bytes, String>(it, StringConverter) } ?: emptyList(),
              messageType?.build() ?: emptyList(),
              enumType?.build() ?: emptyList(),
              service?.build() ?: emptyList(),
              extension?.build() ?: emptyList(),
              options,
              sourceCodeInfo,
              publicDependency?.build() ?: emptyList(),
              weakDependency?.build() ?: emptyList(),
              syntax?.let { LazyReference(it, StringConverter) },
              edition,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            `package` = StringConverter.readValidatedBytes(reader)
          }

          26u -> {
            dependency =
              (dependency ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(LazyReference(StringConverter.readValidatedBytes(reader), StringConverter))
                }
              }
          }

          34u -> {
            messageType =
              (messageType ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(DescriptorProto))
                }
              }
          }

          42u -> {
            enumType =
              (enumType ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(EnumDescriptorProto))
                }
              }
          }

          50u -> {
            service =
              (service ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(ServiceDescriptorProto))
                }
              }
          }

          58u -> {
            extension =
              (extension ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FieldDescriptorProto))
                }
              }
          }

          66u -> {
            options = reader.readMessage(FileOptions)
          }

          74u -> {
            sourceCodeInfo = reader.readMessage(SourceCodeInfo)
          }

          80u -> {
            publicDependency =
              (publicDependency ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readInt32())
                }
              }
          }

          82u -> {
            publicDependency =
              (publicDependency ?: listBuilder()).apply {
                reader.readRepeated(true) {
                  add(reader.readInt32())
                }
              }
          }

          88u -> {
            weakDependency =
              (weakDependency ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readInt32())
                }
              }
          }

          90u -> {
            weakDependency =
              (weakDependency ?: listBuilder()).apply {
                reader.readRepeated(true) {
                  add(reader.readInt32())
                }
              }
          }

          98u -> {
            syntax = StringConverter.readValidatedBytes(reader)
          }

          112u -> {
            edition = reader.readEnum(Edition)
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
    public operator fun invoke(dsl: Builder.() -> Unit): FileDescriptorProto =
      Builder().apply(dsl).build()
  }
}

/**
 * Describes a message type.
 */
@GeneratedMessage("google.protobuf.DescriptorProto")
public class DescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  @GeneratedProperty(2)
  public val `field`: List<FieldDescriptorProto>,
  @GeneratedProperty(3)
  public val nestedType: List<DescriptorProto>,
  @GeneratedProperty(4)
  public val enumType: List<EnumDescriptorProto>,
  @GeneratedProperty(5)
  public val extensionRange: List<ExtensionRange>,
  @GeneratedProperty(6)
  public val extension: List<FieldDescriptorProto>,
  @GeneratedProperty(7)
  public val options: MessageOptions?,
  @GeneratedProperty(8)
  public val oneofDecl: List<OneofDescriptorProto>,
  @GeneratedProperty(9)
  public val reservedRange: List<ReservedRange>,
  /**
   * Reserved field names, which may not be used by fields in the same message. A given name may only be reserved once.
   */
  @GeneratedProperty(10)
  public val reservedName: List<String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (`field`.isNotEmpty()) {
      result += (sizeOf(18u) * `field`.size) + `field`.sumOf { sizeOf(it) }
    }
    if (nestedType.isNotEmpty()) {
      result += (sizeOf(26u) * nestedType.size) + nestedType.sumOf { sizeOf(it) }
    }
    if (enumType.isNotEmpty()) {
      result += (sizeOf(34u) * enumType.size) + enumType.sumOf { sizeOf(it) }
    }
    if (extensionRange.isNotEmpty()) {
      result += (sizeOf(42u) * extensionRange.size) + extensionRange.sumOf { sizeOf(it) }
    }
    if (extension.isNotEmpty()) {
      result += (sizeOf(50u) * extension.size) + extension.sumOf { sizeOf(it) }
    }
    if (options != null) {
      result += sizeOf(58u) + sizeOf(options)
    }
    if (oneofDecl.isNotEmpty()) {
      result += (sizeOf(66u) * oneofDecl.size) + oneofDecl.sumOf { sizeOf(it) }
    }
    if (reservedRange.isNotEmpty()) {
      result += (sizeOf(74u) * reservedRange.size) + reservedRange.sumOf { sizeOf(it) }
    }
    if (reservedName.isNotEmpty()) {
      result +=
        @Suppress("UNCHECKED_CAST")
        (reservedName as LazyConvertingList<Bytes, Any>).let { list ->
          (sizeOf(82u) * list.size) +
            run {
              var sum = 0
              for (i in list.indices) sum += sizeOf(list.wireGet(i))
              sum
            }
        }
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    `field`.forEach { writer.writeTag(18u).write(it) }
    nestedType.forEach { writer.writeTag(26u).write(it) }
    enumType.forEach { writer.writeTag(34u).write(it) }
    extensionRange.forEach { writer.writeTag(42u).write(it) }
    extension.forEach { writer.writeTag(50u).write(it) }
    if (options != null) {
      writer.writeTag(58u).write(options)
    }
    oneofDecl.forEach { writer.writeTag(66u).write(it) }
    reservedRange.forEach { writer.writeTag(74u).write(it) }
    if (reservedName.isNotEmpty()) {
      @Suppress("UNCHECKED_CAST")
      (reservedName as LazyConvertingList<Bytes, Any>).wireForEach { writer.writeTag(82u).write(it) }
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is DescriptorProto &&
      other.name == this.name &&
      other.`field` == this.`field` &&
      other.nestedType == this.nestedType &&
      other.enumType == this.enumType &&
      other.extensionRange == this.extensionRange &&
      other.extension == this.extension &&
      other.options == this.options &&
      other.oneofDecl == this.oneofDecl &&
      other.reservedRange == this.reservedRange &&
      other.reservedName == this.reservedName &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + `field`.hashCode()
    result = 31 * result + nestedType.hashCode()
    result = 31 * result + enumType.hashCode()
    result = 31 * result + extensionRange.hashCode()
    result = 31 * result + extension.hashCode()
    result = 31 * result + options.hashCode()
    result = 31 * result + oneofDecl.hashCode()
    result = 31 * result + reservedRange.hashCode()
    result = 31 * result + reservedName.hashCode()
    return result
  }

  override fun toString(): String =
    "DescriptorProto(" +
      "name=$name, " +
      "`field`=$`field`, " +
      "nestedType=$nestedType, " +
      "enumType=$enumType, " +
      "extensionRange=$extensionRange, " +
      "extension=$extension, " +
      "options=$options, " +
      "oneofDecl=$oneofDecl, " +
      "reservedRange=$reservedRange, " +
      "reservedName=$reservedName" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): DescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var `field`: List<FieldDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var nestedType: List<DescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var enumType: List<EnumDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var extensionRange: List<ExtensionRange> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var extension: List<FieldDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var options: MessageOptions? = null

    public var oneofDecl: List<OneofDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var reservedRange: List<ReservedRange> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var reservedName: List<String> = emptyList()
      set(newValue) {
        field = if (newValue is LazyConvertingList<*, *>) newValue else LazyConvertingList.fromKotlin(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): DescriptorProto =
      DescriptorProto(
        _nameRef,
        freezeList(`field`),
        freezeList(nestedType),
        freezeList(enumType),
        freezeList(extensionRange),
        freezeList(extension),
        options,
        freezeList(oneofDecl),
        freezeList(reservedRange),
        reservedName,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: DescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it.`field` = msg.`field`
          it.nestedType = msg.nestedType
          it.enumType = msg.enumType
          it.extensionRange = msg.extensionRange
          it.extension = msg.extension
          it.options = msg.options
          it.oneofDecl = msg.oneofDecl
          it.reservedRange = msg.reservedRange
          it.reservedName = msg.reservedName
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<DescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): DescriptorProto {
      var name: Bytes? = null
      var `field`: ListBuilder<FieldDescriptorProto>? = null
      var nestedType: ListBuilder<DescriptorProto>? = null
      var enumType: ListBuilder<EnumDescriptorProto>? = null
      var extensionRange: ListBuilder<ExtensionRange>? = null
      var extension: ListBuilder<FieldDescriptorProto>? = null
      var options: MessageOptions? = null
      var oneofDecl: ListBuilder<OneofDescriptorProto>? = null
      var reservedRange: ListBuilder<ReservedRange>? = null
      var reservedName: ListBuilder<Any?>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return DescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              `field`?.build() ?: emptyList(),
              nestedType?.build() ?: emptyList(),
              enumType?.build() ?: emptyList(),
              extensionRange?.build() ?: emptyList(),
              extension?.build() ?: emptyList(),
              options,
              oneofDecl?.build() ?: emptyList(),
              reservedRange?.build() ?: emptyList(),
              reservedName?.build()?.let { LazyConvertingList<Bytes, String>(it, StringConverter) } ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            `field` =
              (`field` ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FieldDescriptorProto))
                }
              }
          }

          26u -> {
            nestedType =
              (nestedType ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(DescriptorProto))
                }
              }
          }

          34u -> {
            enumType =
              (enumType ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(EnumDescriptorProto))
                }
              }
          }

          42u -> {
            extensionRange =
              (extensionRange ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(ExtensionRange))
                }
              }
          }

          50u -> {
            extension =
              (extension ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FieldDescriptorProto))
                }
              }
          }

          58u -> {
            options = reader.readMessage(MessageOptions)
          }

          66u -> {
            oneofDecl =
              (oneofDecl ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(OneofDescriptorProto))
                }
              }
          }

          74u -> {
            reservedRange =
              (reservedRange ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(ReservedRange))
                }
              }
          }

          82u -> {
            reservedName =
              (reservedName ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(LazyReference(StringConverter.readValidatedBytes(reader), StringConverter))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): DescriptorProto =
      Builder().apply(dsl).build()
  }

  @GeneratedMessage("google.protobuf.DescriptorProto.ExtensionRange")
  public class ExtensionRange private constructor(
    @GeneratedProperty(1)
    public val start: Int?,
    @GeneratedProperty(2)
    public val end: Int?,
    @GeneratedProperty(3)
    public val options: ExtensionRangeOptions?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (start != null) {
        result += sizeOf(8u) + sizeOf(start)
      }
      if (end != null) {
        result += sizeOf(16u) + sizeOf(end)
      }
      if (options != null) {
        result += sizeOf(26u) + sizeOf(options)
      }
      result += unknownFields.size()
      result
    }

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (start != null) {
        writer.writeTag(8u).write(start)
      }
      if (end != null) {
        writer.writeTag(16u).write(end)
      }
      if (options != null) {
        writer.writeTag(26u).write(options)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is ExtensionRange &&
        other.start == this.start &&
        other.end == this.end &&
        other.options == this.options &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + start.hashCode()
      result = 31 * result + end.hashCode()
      result = 31 * result + options.hashCode()
      return result
    }

    override fun toString(): String =
      "ExtensionRange(" +
        "start=$start, " +
        "end=$end, " +
        "options=$options" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): ExtensionRange =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var start: Int? = null

      public var end: Int? = null

      public var options: ExtensionRangeOptions? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): ExtensionRange =
        ExtensionRange(
          start,
          end,
          options,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: ExtensionRange): Builder =
          Builder().also {
            it.start = msg.start
            it.end = msg.end
            it.options = msg.options
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<ExtensionRange>() {
      @JvmStatic
      override fun deserialize(reader: Reader): ExtensionRange {
        var start: Int? = null
        var end: Int? = null
        var options: ExtensionRangeOptions? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return ExtensionRange(
                start,
                end,
                options,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              start = reader.readInt32()
            }

            16u -> {
              end = reader.readInt32()
            }

            26u -> {
              options = reader.readMessage(ExtensionRangeOptions)
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
      public operator fun invoke(dsl: Builder.() -> Unit): ExtensionRange =
        Builder().apply(dsl).build()
    }
  }

  /**
   * Range of reserved tag numbers. Reserved tag numbers may not be used by fields or extension ranges in the same message. Reserved ranges may not overlap.
   */
  @GeneratedMessage("google.protobuf.DescriptorProto.ReservedRange")
  public class ReservedRange private constructor(
    @GeneratedProperty(1)
    public val start: Int?,
    @GeneratedProperty(2)
    public val end: Int?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (start != null) {
        result += sizeOf(8u) + sizeOf(start)
      }
      if (end != null) {
        result += sizeOf(16u) + sizeOf(end)
      }
      result += unknownFields.size()
      result
    }

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (start != null) {
        writer.writeTag(8u).write(start)
      }
      if (end != null) {
        writer.writeTag(16u).write(end)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is ReservedRange &&
        other.start == this.start &&
        other.end == this.end &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + start.hashCode()
      result = 31 * result + end.hashCode()
      return result
    }

    override fun toString(): String =
      "ReservedRange(" +
        "start=$start, " +
        "end=$end" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): ReservedRange =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var start: Int? = null

      public var end: Int? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): ReservedRange =
        ReservedRange(
          start,
          end,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: ReservedRange): Builder =
          Builder().also {
            it.start = msg.start
            it.end = msg.end
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<ReservedRange>() {
      @JvmStatic
      override fun deserialize(reader: Reader): ReservedRange {
        var start: Int? = null
        var end: Int? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return ReservedRange(
                start,
                end,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              start = reader.readInt32()
            }

            16u -> {
              end = reader.readInt32()
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
      public operator fun invoke(dsl: Builder.() -> Unit): ReservedRange =
        Builder().apply(dsl).build()
    }
  }
}

@GeneratedMessage("google.protobuf.ExtensionRangeOptions")
public class ExtensionRangeOptions private constructor(
  /**
   * For external users: DO NOT USE. We are in the process of open sourcing extension declaration and executing internal cleanups before it can be used externally.
   */
  @GeneratedProperty(2)
  public val declaration: List<Declaration>,
  /**
   * The verification state of the range. TODO: flip the default to DECLARATION once all empty ranges are marked as UNVERIFIED.
   */
  @GeneratedProperty(3)
  public val verification: VerificationState?,
  /**
   * Any features defined in the specific edition.
   */
  @GeneratedProperty(50)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (declaration.isNotEmpty()) {
      result += (sizeOf(18u) * declaration.size) + declaration.sumOf { sizeOf(it) }
    }
    if (verification != null) {
      result += sizeOf(24u) + sizeOf(verification)
    }
    if (features != null) {
      result += sizeOf(402u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    declaration.forEach { writer.writeTag(18u).write(it) }
    if (verification != null) {
      writer.writeTag(24u).write(verification)
    }
    if (features != null) {
      writer.writeTag(402u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is ExtensionRangeOptions &&
      other.declaration == this.declaration &&
      other.verification == this.verification &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + declaration.hashCode()
    result = 31 * result + verification.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "ExtensionRangeOptions(" +
      "declaration=$declaration, " +
      "verification=$verification, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): ExtensionRangeOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var declaration: List<Declaration> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var verification: VerificationState? = null

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): ExtensionRangeOptions =
      ExtensionRangeOptions(
        freezeList(declaration),
        verification,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: ExtensionRangeOptions): Builder =
        Builder().also {
          it.declaration = msg.declaration
          it.verification = msg.verification
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<ExtensionRangeOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): ExtensionRangeOptions {
      var declaration: ListBuilder<Declaration>? = null
      var verification: VerificationState? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return ExtensionRangeOptions(
              declaration?.build() ?: emptyList(),
              verification,
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          18u -> {
            declaration =
              (declaration ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(Declaration))
                }
              }
          }

          24u -> {
            verification = reader.readEnum(VerificationState)
          }

          402u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): ExtensionRangeOptions =
      Builder().apply(dsl).build()
  }

  /**
   * The verification state of the extension range.
   */
  public sealed class VerificationState(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    /**
     * All the extensions of the range must be declared.
     */
    public object DECLARATION : VerificationState(0, "DECLARATION")

    public object UNVERIFIED : VerificationState(1, "UNVERIFIED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : VerificationState(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<VerificationState> {
      override fun deserialize(`value`: Int): VerificationState =
        when (value) {
          0 -> DECLARATION
          1 -> UNVERIFIED
          else -> UNRECOGNIZED(value)
        }
    }
  }

  @GeneratedMessage("google.protobuf.ExtensionRangeOptions.Declaration")
  public class Declaration private constructor(
    /**
     * The extension number declared within the extension range.
     */
    @GeneratedProperty(1)
    public val number: Int?,
    private val _fullName: LazyReference<Bytes, String>?,
    private val _type: LazyReference<Bytes, String>?,
    /**
     * If true, indicates that the number is reserved in the extension range, and any extension field with the number will fail to compile. Set this when a declared extension field is deleted.
     */
    @GeneratedProperty(5)
    public val reserved: Boolean?,
    /**
     * If true, indicates that the extension must be defined as repeated. Otherwise the extension must be defined as optional.
     */
    @GeneratedProperty(6)
    public val repeated: Boolean?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (number != null) {
        result += sizeOf(8u) + sizeOf(number)
      }
      if (_fullName != null) {
        result += sizeOf(18u) + sizeOf(_fullName.wireValue())
      }
      if (_type != null) {
        result += sizeOf(26u) + sizeOf(_type.wireValue())
      }
      if (reserved != null) {
        result += sizeOf(40u) + 1
      }
      if (repeated != null) {
        result += sizeOf(48u) + 1
      }
      result += unknownFields.size()
      result
    }

    /**
     * The fully-qualified name of the extension field. There must be a leading dot in front of the full name.
     */
    @GeneratedProperty(2)
    public val fullName: String?
      get() = _fullName?.value()

    /**
     * The fully-qualified type name of the extension field. Unlike Metadata.type, Declaration.type must have a leading dot for messages and enums.
     */
    @GeneratedProperty(3)
    public val type: String?
      get() = _type?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (number != null) {
        writer.writeTag(8u).write(number)
      }
      if (_fullName != null) {
        writer.writeTag(18u).write(_fullName.wireValue())
      }
      if (_type != null) {
        writer.writeTag(26u).write(_type.wireValue())
      }
      if (reserved != null) {
        writer.writeTag(40u).write(reserved)
      }
      if (repeated != null) {
        writer.writeTag(48u).write(repeated)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is Declaration &&
        other.number == this.number &&
        other.fullName == this.fullName &&
        other.type == this.type &&
        other.reserved == this.reserved &&
        other.repeated == this.repeated &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + number.hashCode()
      result = 31 * result + fullName.hashCode()
      result = 31 * result + type.hashCode()
      result = 31 * result + reserved.hashCode()
      result = 31 * result + repeated.hashCode()
      return result
    }

    override fun toString(): String =
      "Declaration(" +
        "number=$number, " +
        "fullName=$fullName, " +
        "type=$type, " +
        "reserved=$reserved, " +
        "repeated=$repeated" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): Declaration =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var number: Int? = null

      private var _fullNameRef: LazyReference<Bytes, String>? = null

      public var fullName: String?
        get() = _fullNameRef?.value()
        set(newValue) {
          _fullNameRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      private var _typeRef: LazyReference<Bytes, String>? = null

      public var type: String?
        get() = _typeRef?.value()
        set(newValue) {
          _typeRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var reserved: Boolean? = null

      public var repeated: Boolean? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): Declaration =
        Declaration(
          number,
          _fullNameRef,
          _typeRef,
          reserved,
          repeated,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: Declaration): Builder =
          Builder().also {
            it.number = msg.number
            it._fullNameRef = msg._fullName
            it._typeRef = msg._type
            it.reserved = msg.reserved
            it.repeated = msg.repeated
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<Declaration>() {
      @JvmStatic
      override fun deserialize(reader: Reader): Declaration {
        var number: Int? = null
        var fullName: Bytes? = null
        var type: Bytes? = null
        var reserved: Boolean? = null
        var repeated: Boolean? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return Declaration(
                number,
                fullName?.let { LazyReference(it, StringConverter) },
                type?.let { LazyReference(it, StringConverter) },
                reserved,
                repeated,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              number = reader.readInt32()
            }

            18u -> {
              fullName = StringConverter.readValidatedBytes(reader)
            }

            26u -> {
              type = StringConverter.readValidatedBytes(reader)
            }

            40u -> {
              reserved = reader.readBool()
            }

            48u -> {
              repeated = reader.readBool()
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
      public operator fun invoke(dsl: Builder.() -> Unit): Declaration =
        Builder().apply(dsl).build()
    }
  }
}

/**
 * Describes a field within a message.
 */
@GeneratedMessage("google.protobuf.FieldDescriptorProto")
public class FieldDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  private val _extendee: LazyReference<Bytes, String>?,
  @GeneratedProperty(3)
  public val number: Int?,
  @GeneratedProperty(4)
  public val label: Label?,
  /**
   * If type_name is set, this need not be set.  If both this and type_name are set, this must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
   */
  @GeneratedProperty(5)
  public val type: Type?,
  private val _typeName: LazyReference<Bytes, String>?,
  private val _defaultValue: LazyReference<Bytes, String>?,
  @GeneratedProperty(8)
  public val options: FieldOptions?,
  /**
   * If set, gives the index of a oneof in the containing type's oneof_decl list.  This field is a member of that oneof.
   */
  @GeneratedProperty(9)
  public val oneofIndex: Int?,
  private val _jsonName: LazyReference<Bytes, String>?,
  /**
   * If true, this is a proto3 "optional". When a proto3 field is optional, it tracks presence regardless of field type.
   *
   *  When proto3_optional is true, this field must belong to a oneof to signal to old proto3 clients that presence is tracked for this field. This oneof is known as a "synthetic" oneof, and this field must be its sole member (each proto3 optional field gets its own synthetic oneof). Synthetic oneofs exist in the descriptor only, and do not generate any API. Synthetic oneofs must be ordered after all "real" oneofs.
   *
   *  For message fields, proto3_optional doesn't create any semantic change, since non-repeated message fields always track presence. However it still indicates the semantic detail of whether the user wrote "optional" or not. This can be useful for round-tripping the .proto file. For consistency we give message fields a synthetic oneof also, even though it is not required to track presence. This is especially important because the parser can't tell if a field is a message or an enum, so it must always create a synthetic oneof.
   *
   *  Proto2 optional fields do not set this flag, because they already indicate optional with `LABEL_OPTIONAL`.
   */
  @GeneratedProperty(17)
  public val proto3Optional: Boolean?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (_extendee != null) {
      result += sizeOf(18u) + sizeOf(_extendee.wireValue())
    }
    if (number != null) {
      result += sizeOf(24u) + sizeOf(number)
    }
    if (label != null) {
      result += sizeOf(32u) + sizeOf(label)
    }
    if (type != null) {
      result += sizeOf(40u) + sizeOf(type)
    }
    if (_typeName != null) {
      result += sizeOf(50u) + sizeOf(_typeName.wireValue())
    }
    if (_defaultValue != null) {
      result += sizeOf(58u) + sizeOf(_defaultValue.wireValue())
    }
    if (options != null) {
      result += sizeOf(66u) + sizeOf(options)
    }
    if (oneofIndex != null) {
      result += sizeOf(72u) + sizeOf(oneofIndex)
    }
    if (_jsonName != null) {
      result += sizeOf(82u) + sizeOf(_jsonName.wireValue())
    }
    if (proto3Optional != null) {
      result += sizeOf(136u) + 1
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  /**
   * For extensions, this is the name of the type being extended.  It is resolved in the same manner as type_name.
   */
  @GeneratedProperty(2)
  public val extendee: String?
    get() = _extendee?.value()

  /**
   * For message and enum types, this is the name of the type.  If the name starts with a '.', it is fully-qualified.  Otherwise, C++-like scoping rules are used to find the type (i.e. first the nested types within this message are searched, then within the parent, on up to the root namespace).
   */
  @GeneratedProperty(6)
  public val typeName: String?
    get() = _typeName?.value()

  /**
   * For numeric types, contains the original text representation of the value. For booleans, "true" or "false". For strings, contains the default text contents (not escaped in any way). For bytes, contains the C escaped value.  All bytes >= 128 are escaped.
   */
  @GeneratedProperty(7)
  public val defaultValue: String?
    get() = _defaultValue?.value()

  /**
   * JSON name of this field. The value is set by protocol compiler. If the user has set a "json_name" option on this field, that option's value will be used. Otherwise, it's deduced from the field's name by converting it to camelCase.
   */
  @GeneratedProperty(10)
  public val jsonName: String?
    get() = _jsonName?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    if (_extendee != null) {
      writer.writeTag(18u).write(_extendee.wireValue())
    }
    if (number != null) {
      writer.writeTag(24u).write(number)
    }
    if (label != null) {
      writer.writeTag(32u).write(label)
    }
    if (type != null) {
      writer.writeTag(40u).write(type)
    }
    if (_typeName != null) {
      writer.writeTag(50u).write(_typeName.wireValue())
    }
    if (_defaultValue != null) {
      writer.writeTag(58u).write(_defaultValue.wireValue())
    }
    if (options != null) {
      writer.writeTag(66u).write(options)
    }
    if (oneofIndex != null) {
      writer.writeTag(72u).write(oneofIndex)
    }
    if (_jsonName != null) {
      writer.writeTag(82u).write(_jsonName.wireValue())
    }
    if (proto3Optional != null) {
      writer.writeTag(136u).write(proto3Optional)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FieldDescriptorProto &&
      other.name == this.name &&
      other.extendee == this.extendee &&
      other.number == this.number &&
      other.label == this.label &&
      other.type == this.type &&
      other.typeName == this.typeName &&
      other.defaultValue == this.defaultValue &&
      other.options == this.options &&
      other.oneofIndex == this.oneofIndex &&
      other.jsonName == this.jsonName &&
      other.proto3Optional == this.proto3Optional &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + extendee.hashCode()
    result = 31 * result + number.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + typeName.hashCode()
    result = 31 * result + defaultValue.hashCode()
    result = 31 * result + options.hashCode()
    result = 31 * result + oneofIndex.hashCode()
    result = 31 * result + jsonName.hashCode()
    result = 31 * result + proto3Optional.hashCode()
    return result
  }

  override fun toString(): String =
    "FieldDescriptorProto(" +
      "name=$name, " +
      "extendee=$extendee, " +
      "number=$number, " +
      "label=$label, " +
      "type=$type, " +
      "typeName=$typeName, " +
      "defaultValue=$defaultValue, " +
      "options=$options, " +
      "oneofIndex=$oneofIndex, " +
      "jsonName=$jsonName, " +
      "proto3Optional=$proto3Optional" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FieldDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _extendeeRef: LazyReference<Bytes, String>? = null

    public var extendee: String?
      get() = _extendeeRef?.value()
      set(newValue) {
        _extendeeRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var number: Int? = null

    public var label: Label? = null

    public var type: Type? = null

    private var _typeNameRef: LazyReference<Bytes, String>? = null

    public var typeName: String?
      get() = _typeNameRef?.value()
      set(newValue) {
        _typeNameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _defaultValueRef: LazyReference<Bytes, String>? = null

    public var defaultValue: String?
      get() = _defaultValueRef?.value()
      set(newValue) {
        _defaultValueRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var options: FieldOptions? = null

    public var oneofIndex: Int? = null

    private var _jsonNameRef: LazyReference<Bytes, String>? = null

    public var jsonName: String?
      get() = _jsonNameRef?.value()
      set(newValue) {
        _jsonNameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var proto3Optional: Boolean? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FieldDescriptorProto =
      FieldDescriptorProto(
        _nameRef,
        _extendeeRef,
        number,
        label,
        type,
        _typeNameRef,
        _defaultValueRef,
        options,
        oneofIndex,
        _jsonNameRef,
        proto3Optional,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FieldDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it._extendeeRef = msg._extendee
          it.number = msg.number
          it.label = msg.label
          it.type = msg.type
          it._typeNameRef = msg._typeName
          it._defaultValueRef = msg._defaultValue
          it.options = msg.options
          it.oneofIndex = msg.oneofIndex
          it._jsonNameRef = msg._jsonName
          it.proto3Optional = msg.proto3Optional
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FieldDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FieldDescriptorProto {
      var name: Bytes? = null
      var extendee: Bytes? = null
      var number: Int? = null
      var label: Label? = null
      var type: Type? = null
      var typeName: Bytes? = null
      var defaultValue: Bytes? = null
      var options: FieldOptions? = null
      var oneofIndex: Int? = null
      var jsonName: Bytes? = null
      var proto3Optional: Boolean? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FieldDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              extendee?.let { LazyReference(it, StringConverter) },
              number,
              label,
              type,
              typeName?.let { LazyReference(it, StringConverter) },
              defaultValue?.let { LazyReference(it, StringConverter) },
              options,
              oneofIndex,
              jsonName?.let { LazyReference(it, StringConverter) },
              proto3Optional,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            extendee = StringConverter.readValidatedBytes(reader)
          }

          24u -> {
            number = reader.readInt32()
          }

          32u -> {
            label = reader.readEnum(Label)
          }

          40u -> {
            type = reader.readEnum(Type)
          }

          50u -> {
            typeName = StringConverter.readValidatedBytes(reader)
          }

          58u -> {
            defaultValue = StringConverter.readValidatedBytes(reader)
          }

          66u -> {
            options = reader.readMessage(FieldOptions)
          }

          72u -> {
            oneofIndex = reader.readInt32()
          }

          82u -> {
            jsonName = StringConverter.readValidatedBytes(reader)
          }

          136u -> {
            proto3Optional = reader.readBool()
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
    public operator fun invoke(dsl: Builder.() -> Unit): FieldDescriptorProto =
      Builder().apply(dsl).build()
  }

  public sealed class Type(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    /**
     * 0 is reserved for errors. Order is weird for historical reasons.
     */
    public object DOUBLE : Type(1, "DOUBLE")

    public object FLOAT : Type(2, "FLOAT")

    /**
     * Not ZigZag encoded.  Negative numbers take 10 bytes.  Use TYPE_SINT64 if negative values are likely.
     */
    public object INT64 : Type(3, "INT64")

    public object UINT64 : Type(4, "UINT64")

    /**
     * Not ZigZag encoded.  Negative numbers take 10 bytes.  Use TYPE_SINT32 if negative values are likely.
     */
    public object INT32 : Type(5, "INT32")

    public object FIXED64 : Type(6, "FIXED64")

    public object FIXED32 : Type(7, "FIXED32")

    public object BOOL : Type(8, "BOOL")

    public object STRING : Type(9, "STRING")

    /**
     * Tag-delimited aggregate. Group type is deprecated and not supported after google.protobuf. However, Proto3 implementations should still be able to parse the group wire format and treat group fields as unknown fields.  In Editions, the group wire format can be enabled via the `message_encoding` feature.
     */
    public object GROUP : Type(10, "GROUP")

    public object MESSAGE : Type(11, "MESSAGE")

    /**
     * New in version 2.
     */
    public object BYTES : Type(12, "BYTES")

    public object UINT32 : Type(13, "UINT32")

    public object ENUM : Type(14, "ENUM")

    public object SFIXED32 : Type(15, "SFIXED32")

    public object SFIXED64 : Type(16, "SFIXED64")

    public object SINT32 : Type(17, "SINT32")

    public object SINT64 : Type(18, "SINT64")

    public class UNRECOGNIZED(
      `value`: Int
    ) : Type(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<Type> {
      override fun deserialize(`value`: Int): Type =
        when (value) {
          1 -> DOUBLE
          2 -> FLOAT
          3 -> INT64
          4 -> UINT64
          5 -> INT32
          6 -> FIXED64
          7 -> FIXED32
          8 -> BOOL
          9 -> STRING
          10 -> GROUP
          11 -> MESSAGE
          12 -> BYTES
          13 -> UINT32
          14 -> ENUM
          15 -> SFIXED32
          16 -> SFIXED64
          17 -> SINT32
          18 -> SINT64
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class Label(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    /**
     * 0 is reserved for errors
     */
    public object OPTIONAL : Label(1, "OPTIONAL")

    public object REPEATED : Label(3, "REPEATED")

    /**
     * The required label is only allowed in google.protobuf.  In proto3 and Editions it's explicitly prohibited.  In Editions, the `field_presence` feature can be used to get this behavior.
     */
    public object REQUIRED : Label(2, "REQUIRED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : Label(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<Label> {
      override fun deserialize(`value`: Int): Label =
        when (value) {
          1 -> OPTIONAL
          3 -> REPEATED
          2 -> REQUIRED
          else -> UNRECOGNIZED(value)
        }
    }
  }
}

/**
 * Describes a oneof.
 */
@GeneratedMessage("google.protobuf.OneofDescriptorProto")
public class OneofDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  @GeneratedProperty(2)
  public val options: OneofOptions?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (options != null) {
      result += sizeOf(18u) + sizeOf(options)
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    if (options != null) {
      writer.writeTag(18u).write(options)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is OneofDescriptorProto &&
      other.name == this.name &&
      other.options == this.options &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + options.hashCode()
    return result
  }

  override fun toString(): String =
    "OneofDescriptorProto(" +
      "name=$name, " +
      "options=$options" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): OneofDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var options: OneofOptions? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): OneofDescriptorProto =
      OneofDescriptorProto(
        _nameRef,
        options,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: OneofDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it.options = msg.options
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<OneofDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): OneofDescriptorProto {
      var name: Bytes? = null
      var options: OneofOptions? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return OneofDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              options,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            options = reader.readMessage(OneofOptions)
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
    public operator fun invoke(dsl: Builder.() -> Unit): OneofDescriptorProto =
      Builder().apply(dsl).build()
  }
}

/**
 * Describes an enum type.
 */
@GeneratedMessage("google.protobuf.EnumDescriptorProto")
public class EnumDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  @GeneratedProperty(2)
  public val `value`: List<EnumValueDescriptorProto>,
  @GeneratedProperty(3)
  public val options: EnumOptions?,
  /**
   * Range of reserved numeric values. Reserved numeric values may not be used by enum values in the same enum declaration. Reserved ranges may not overlap.
   */
  @GeneratedProperty(4)
  public val reservedRange: List<EnumReservedRange>,
  /**
   * Reserved enum value names, which may not be reused. A given name may only be reserved once.
   */
  @GeneratedProperty(5)
  public val reservedName: List<String>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (`value`.isNotEmpty()) {
      result += (sizeOf(18u) * `value`.size) + `value`.sumOf { sizeOf(it) }
    }
    if (options != null) {
      result += sizeOf(26u) + sizeOf(options)
    }
    if (reservedRange.isNotEmpty()) {
      result += (sizeOf(34u) * reservedRange.size) + reservedRange.sumOf { sizeOf(it) }
    }
    if (reservedName.isNotEmpty()) {
      result +=
        @Suppress("UNCHECKED_CAST")
        (reservedName as LazyConvertingList<Bytes, Any>).let { list ->
          (sizeOf(42u) * list.size) +
            run {
              var sum = 0
              for (i in list.indices) sum += sizeOf(list.wireGet(i))
              sum
            }
        }
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    `value`.forEach { writer.writeTag(18u).write(it) }
    if (options != null) {
      writer.writeTag(26u).write(options)
    }
    reservedRange.forEach { writer.writeTag(34u).write(it) }
    if (reservedName.isNotEmpty()) {
      @Suppress("UNCHECKED_CAST")
      (reservedName as LazyConvertingList<Bytes, Any>).wireForEach { writer.writeTag(42u).write(it) }
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumDescriptorProto &&
      other.name == this.name &&
      other.`value` == this.`value` &&
      other.options == this.options &&
      other.reservedRange == this.reservedRange &&
      other.reservedName == this.reservedName &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + `value`.hashCode()
    result = 31 * result + options.hashCode()
    result = 31 * result + reservedRange.hashCode()
    result = 31 * result + reservedName.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumDescriptorProto(" +
      "name=$name, " +
      "`value`=$`value`, " +
      "options=$options, " +
      "reservedRange=$reservedRange, " +
      "reservedName=$reservedName" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var `value`: List<EnumValueDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var options: EnumOptions? = null

    public var reservedRange: List<EnumReservedRange> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var reservedName: List<String> = emptyList()
      set(newValue) {
        field = if (newValue is LazyConvertingList<*, *>) newValue else LazyConvertingList.fromKotlin(newValue, StringConverter)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumDescriptorProto =
      EnumDescriptorProto(
        _nameRef,
        freezeList(`value`),
        options,
        freezeList(reservedRange),
        reservedName,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it.`value` = msg.`value`
          it.options = msg.options
          it.reservedRange = msg.reservedRange
          it.reservedName = msg.reservedName
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumDescriptorProto {
      var name: Bytes? = null
      var `value`: ListBuilder<EnumValueDescriptorProto>? = null
      var options: EnumOptions? = null
      var reservedRange: ListBuilder<EnumReservedRange>? = null
      var reservedName: ListBuilder<Any?>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              `value`?.build() ?: emptyList(),
              options,
              reservedRange?.build() ?: emptyList(),
              reservedName?.build()?.let { LazyConvertingList<Bytes, String>(it, StringConverter) } ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            `value` =
              (`value` ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(EnumValueDescriptorProto))
                }
              }
          }

          26u -> {
            options = reader.readMessage(EnumOptions)
          }

          34u -> {
            reservedRange =
              (reservedRange ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(EnumReservedRange))
                }
              }
          }

          42u -> {
            reservedName =
              (reservedName ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(LazyReference(StringConverter.readValidatedBytes(reader), StringConverter))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): EnumDescriptorProto =
      Builder().apply(dsl).build()
  }

  /**
   * Range of reserved numeric values. Reserved values may not be used by entries in the same enum. Reserved ranges may not overlap.
   *
   *  Note that this is distinct from DescriptorProto.ReservedRange in that it is inclusive such that it can appropriately represent the entire int32 domain.
   */
  @GeneratedMessage("google.protobuf.EnumDescriptorProto.EnumReservedRange")
  public class EnumReservedRange private constructor(
    @GeneratedProperty(1)
    public val start: Int?,
    @GeneratedProperty(2)
    public val end: Int?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (start != null) {
        result += sizeOf(8u) + sizeOf(start)
      }
      if (end != null) {
        result += sizeOf(16u) + sizeOf(end)
      }
      result += unknownFields.size()
      result
    }

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (start != null) {
        writer.writeTag(8u).write(start)
      }
      if (end != null) {
        writer.writeTag(16u).write(end)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is EnumReservedRange &&
        other.start == this.start &&
        other.end == this.end &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + start.hashCode()
      result = 31 * result + end.hashCode()
      return result
    }

    override fun toString(): String =
      "EnumReservedRange(" +
        "start=$start, " +
        "end=$end" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): EnumReservedRange =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var start: Int? = null

      public var end: Int? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): EnumReservedRange =
        EnumReservedRange(
          start,
          end,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: EnumReservedRange): Builder =
          Builder().also {
            it.start = msg.start
            it.end = msg.end
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<EnumReservedRange>() {
      @JvmStatic
      override fun deserialize(reader: Reader): EnumReservedRange {
        var start: Int? = null
        var end: Int? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return EnumReservedRange(
                start,
                end,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              start = reader.readInt32()
            }

            16u -> {
              end = reader.readInt32()
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
      public operator fun invoke(dsl: Builder.() -> Unit): EnumReservedRange =
        Builder().apply(dsl).build()
    }
  }
}

/**
 * Describes a value within an enum.
 */
@GeneratedMessage("google.protobuf.EnumValueDescriptorProto")
public class EnumValueDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  @GeneratedProperty(2)
  public val number: Int?,
  @GeneratedProperty(3)
  public val options: EnumValueOptions?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (number != null) {
      result += sizeOf(16u) + sizeOf(number)
    }
    if (options != null) {
      result += sizeOf(26u) + sizeOf(options)
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    if (number != null) {
      writer.writeTag(16u).write(number)
    }
    if (options != null) {
      writer.writeTag(26u).write(options)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumValueDescriptorProto &&
      other.name == this.name &&
      other.number == this.number &&
      other.options == this.options &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + number.hashCode()
    result = 31 * result + options.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumValueDescriptorProto(" +
      "name=$name, " +
      "number=$number, " +
      "options=$options" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumValueDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var number: Int? = null

    public var options: EnumValueOptions? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumValueDescriptorProto =
      EnumValueDescriptorProto(
        _nameRef,
        number,
        options,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumValueDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it.number = msg.number
          it.options = msg.options
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumValueDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumValueDescriptorProto {
      var name: Bytes? = null
      var number: Int? = null
      var options: EnumValueOptions? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumValueDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              number,
              options,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          16u -> {
            number = reader.readInt32()
          }

          26u -> {
            options = reader.readMessage(EnumValueOptions)
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
    public operator fun invoke(dsl: Builder.() -> Unit): EnumValueDescriptorProto =
      Builder().apply(dsl).build()
  }
}

/**
 * Describes a service.
 */
@GeneratedMessage("google.protobuf.ServiceDescriptorProto")
public class ServiceDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  @GeneratedProperty(2)
  public val method: List<MethodDescriptorProto>,
  @GeneratedProperty(3)
  public val options: ServiceOptions?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (method.isNotEmpty()) {
      result += (sizeOf(18u) * method.size) + method.sumOf { sizeOf(it) }
    }
    if (options != null) {
      result += sizeOf(26u) + sizeOf(options)
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    method.forEach { writer.writeTag(18u).write(it) }
    if (options != null) {
      writer.writeTag(26u).write(options)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is ServiceDescriptorProto &&
      other.name == this.name &&
      other.method == this.method &&
      other.options == this.options &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + method.hashCode()
    result = 31 * result + options.hashCode()
    return result
  }

  override fun toString(): String =
    "ServiceDescriptorProto(" +
      "name=$name, " +
      "method=$method, " +
      "options=$options" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): ServiceDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var method: List<MethodDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var options: ServiceOptions? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): ServiceDescriptorProto =
      ServiceDescriptorProto(
        _nameRef,
        freezeList(method),
        options,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: ServiceDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it.method = msg.method
          it.options = msg.options
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<ServiceDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): ServiceDescriptorProto {
      var name: Bytes? = null
      var method: ListBuilder<MethodDescriptorProto>? = null
      var options: ServiceOptions? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return ServiceDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              method?.build() ?: emptyList(),
              options,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            method =
              (method ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(MethodDescriptorProto))
                }
              }
          }

          26u -> {
            options = reader.readMessage(ServiceOptions)
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
    public operator fun invoke(dsl: Builder.() -> Unit): ServiceDescriptorProto =
      Builder().apply(dsl).build()
  }
}

/**
 * Describes a method of a service.
 */
@GeneratedMessage("google.protobuf.MethodDescriptorProto")
public class MethodDescriptorProto private constructor(
  private val _name: LazyReference<Bytes, String>?,
  private val _inputType: LazyReference<Bytes, String>?,
  private val _outputType: LazyReference<Bytes, String>?,
  @GeneratedProperty(4)
  public val options: MethodOptions?,
  /**
   * Identifies if client streams multiple client messages
   */
  @GeneratedProperty(5)
  public val clientStreaming: Boolean?,
  /**
   * Identifies if server streams multiple server messages
   */
  @GeneratedProperty(6)
  public val serverStreaming: Boolean?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_name != null) {
      result += sizeOf(10u) + sizeOf(_name.wireValue())
    }
    if (_inputType != null) {
      result += sizeOf(18u) + sizeOf(_inputType.wireValue())
    }
    if (_outputType != null) {
      result += sizeOf(26u) + sizeOf(_outputType.wireValue())
    }
    if (options != null) {
      result += sizeOf(34u) + sizeOf(options)
    }
    if (clientStreaming != null) {
      result += sizeOf(40u) + 1
    }
    if (serverStreaming != null) {
      result += sizeOf(48u) + 1
    }
    result += unknownFields.size()
    result
  }

  @GeneratedProperty(1)
  public val name: String?
    get() = _name?.value()

  /**
   * Input and output type names.  These are resolved in the same way as FieldDescriptorProto.type_name, but must refer to a message type.
   */
  @GeneratedProperty(2)
  public val inputType: String?
    get() = _inputType?.value()

  @GeneratedProperty(3)
  public val outputType: String?
    get() = _outputType?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_name != null) {
      writer.writeTag(10u).write(_name.wireValue())
    }
    if (_inputType != null) {
      writer.writeTag(18u).write(_inputType.wireValue())
    }
    if (_outputType != null) {
      writer.writeTag(26u).write(_outputType.wireValue())
    }
    if (options != null) {
      writer.writeTag(34u).write(options)
    }
    if (clientStreaming != null) {
      writer.writeTag(40u).write(clientStreaming)
    }
    if (serverStreaming != null) {
      writer.writeTag(48u).write(serverStreaming)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is MethodDescriptorProto &&
      other.name == this.name &&
      other.inputType == this.inputType &&
      other.outputType == this.outputType &&
      other.options == this.options &&
      other.clientStreaming == this.clientStreaming &&
      other.serverStreaming == this.serverStreaming &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + inputType.hashCode()
    result = 31 * result + outputType.hashCode()
    result = 31 * result + options.hashCode()
    result = 31 * result + clientStreaming.hashCode()
    result = 31 * result + serverStreaming.hashCode()
    return result
  }

  override fun toString(): String =
    "MethodDescriptorProto(" +
      "name=$name, " +
      "inputType=$inputType, " +
      "outputType=$outputType, " +
      "options=$options, " +
      "clientStreaming=$clientStreaming, " +
      "serverStreaming=$serverStreaming" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): MethodDescriptorProto =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _nameRef: LazyReference<Bytes, String>? = null

    public var name: String?
      get() = _nameRef?.value()
      set(newValue) {
        _nameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _inputTypeRef: LazyReference<Bytes, String>? = null

    public var inputType: String?
      get() = _inputTypeRef?.value()
      set(newValue) {
        _inputTypeRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _outputTypeRef: LazyReference<Bytes, String>? = null

    public var outputType: String?
      get() = _outputTypeRef?.value()
      set(newValue) {
        _outputTypeRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var options: MethodOptions? = null

    public var clientStreaming: Boolean? = null

    public var serverStreaming: Boolean? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): MethodDescriptorProto =
      MethodDescriptorProto(
        _nameRef,
        _inputTypeRef,
        _outputTypeRef,
        options,
        clientStreaming,
        serverStreaming,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: MethodDescriptorProto): Builder =
        Builder().also {
          it._nameRef = msg._name
          it._inputTypeRef = msg._inputType
          it._outputTypeRef = msg._outputType
          it.options = msg.options
          it.clientStreaming = msg.clientStreaming
          it.serverStreaming = msg.serverStreaming
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<MethodDescriptorProto>() {
    @JvmStatic
    override fun deserialize(reader: Reader): MethodDescriptorProto {
      var name: Bytes? = null
      var inputType: Bytes? = null
      var outputType: Bytes? = null
      var options: MethodOptions? = null
      var clientStreaming: Boolean? = null
      var serverStreaming: Boolean? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return MethodDescriptorProto(
              name?.let { LazyReference(it, StringConverter) },
              inputType?.let { LazyReference(it, StringConverter) },
              outputType?.let { LazyReference(it, StringConverter) },
              options,
              clientStreaming,
              serverStreaming,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            name = StringConverter.readValidatedBytes(reader)
          }

          18u -> {
            inputType = StringConverter.readValidatedBytes(reader)
          }

          26u -> {
            outputType = StringConverter.readValidatedBytes(reader)
          }

          34u -> {
            options = reader.readMessage(MethodOptions)
          }

          40u -> {
            clientStreaming = reader.readBool()
          }

          48u -> {
            serverStreaming = reader.readBool()
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
    public operator fun invoke(dsl: Builder.() -> Unit): MethodDescriptorProto =
      Builder().apply(dsl).build()
  }
}

@GeneratedMessage("google.protobuf.FileOptions")
public class FileOptions private constructor(
  private val _javaPackage: LazyReference<Bytes, String>?,
  private val _javaOuterClassname: LazyReference<Bytes, String>?,
  @GeneratedProperty(9)
  public val optimizeFor: OptimizeMode?,
  /**
   * If enabled, then the Java code generator will generate a separate .java file for each top-level message, enum, and service defined in the .proto file.  Thus, these types will *not* be nested inside the wrapper class named by java_outer_classname.  However, the wrapper class will still be generated to contain the file's getDescriptor() method as well as any top-level extensions defined in the file.
   */
  @GeneratedProperty(10)
  public val javaMultipleFiles: Boolean?,
  private val _goPackage: LazyReference<Bytes, String>?,
  /**
   * Should generic services be generated in each language?  "Generic" services are not specific to any particular RPC system.  They are generated by the main code generators in each language (without additional plugins). Generic services were the only kind of service generation supported by early versions of google.protobuf.
   *
   *  Generic services are now considered deprecated in favor of using plugins that generate code specific to your particular RPC system.  Therefore, these default to false.  Old code which depends on generic services should explicitly set them to true.
   */
  @GeneratedProperty(16)
  public val ccGenericServices: Boolean?,
  @GeneratedProperty(17)
  public val javaGenericServices: Boolean?,
  @GeneratedProperty(18)
  public val pyGenericServices: Boolean?,
  /**
   * This option does nothing.
   */
  @GeneratedProperty(20)
  @Deprecated("deprecated in proto")
  public val javaGenerateEqualsAndHash: Boolean?,
  /**
   * Is this file deprecated? Depending on the target platform, this can emit Deprecated annotations for everything in the file, or it will be completely ignored; in the very least, this is a formalization for deprecating files.
   */
  @GeneratedProperty(23)
  public val deprecated: Boolean?,
  /**
   * A proto2 file can set this to true to opt in to UTF-8 checking for Java, which will throw an exception if invalid UTF-8 is parsed from the wire or assigned to a string field.
   *
   *  TODO: clarify exactly what kinds of field types this option applies to, and update these docs accordingly.
   *
   *  Proto3 files already perform these checks. Setting the option explicitly to false has no effect: it cannot be used to opt proto3 files out of UTF-8 checks.
   */
  @GeneratedProperty(27)
  public val javaStringCheckUtf8: Boolean?,
  /**
   * Enables the use of arenas for the proto messages in this file. This applies only to generated classes for C++.
   */
  @GeneratedProperty(31)
  public val ccEnableArenas: Boolean?,
  private val _objcClassPrefix: LazyReference<Bytes, String>?,
  private val _csharpNamespace: LazyReference<Bytes, String>?,
  private val _swiftPrefix: LazyReference<Bytes, String>?,
  private val _phpClassPrefix: LazyReference<Bytes, String>?,
  private val _phpNamespace: LazyReference<Bytes, String>?,
  private val _phpMetadataNamespace: LazyReference<Bytes, String>?,
  private val _rubyPackage: LazyReference<Bytes, String>?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(50)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See the documentation for the "Options" section above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_javaPackage != null) {
      result += sizeOf(10u) + sizeOf(_javaPackage.wireValue())
    }
    if (_javaOuterClassname != null) {
      result += sizeOf(66u) + sizeOf(_javaOuterClassname.wireValue())
    }
    if (optimizeFor != null) {
      result += sizeOf(72u) + sizeOf(optimizeFor)
    }
    if (javaMultipleFiles != null) {
      result += sizeOf(80u) + 1
    }
    if (_goPackage != null) {
      result += sizeOf(90u) + sizeOf(_goPackage.wireValue())
    }
    if (ccGenericServices != null) {
      result += sizeOf(128u) + 1
    }
    if (javaGenericServices != null) {
      result += sizeOf(136u) + 1
    }
    if (pyGenericServices != null) {
      result += sizeOf(144u) + 1
    }
    if (javaGenerateEqualsAndHash != null) {
      result += sizeOf(160u) + 1
    }
    if (deprecated != null) {
      result += sizeOf(184u) + 1
    }
    if (javaStringCheckUtf8 != null) {
      result += sizeOf(216u) + 1
    }
    if (ccEnableArenas != null) {
      result += sizeOf(248u) + 1
    }
    if (_objcClassPrefix != null) {
      result += sizeOf(290u) + sizeOf(_objcClassPrefix.wireValue())
    }
    if (_csharpNamespace != null) {
      result += sizeOf(298u) + sizeOf(_csharpNamespace.wireValue())
    }
    if (_swiftPrefix != null) {
      result += sizeOf(314u) + sizeOf(_swiftPrefix.wireValue())
    }
    if (_phpClassPrefix != null) {
      result += sizeOf(322u) + sizeOf(_phpClassPrefix.wireValue())
    }
    if (_phpNamespace != null) {
      result += sizeOf(330u) + sizeOf(_phpNamespace.wireValue())
    }
    if (_phpMetadataNamespace != null) {
      result += sizeOf(354u) + sizeOf(_phpMetadataNamespace.wireValue())
    }
    if (_rubyPackage != null) {
      result += sizeOf(362u) + sizeOf(_rubyPackage.wireValue())
    }
    if (features != null) {
      result += sizeOf(402u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  /**
   * Sets the Java package where classes generated from this .proto will be placed.  By default, the proto package is used, but this is often inappropriate because proto packages do not normally start with backwards domain names.
   */
  @GeneratedProperty(1)
  public val javaPackage: String?
    get() = _javaPackage?.value()

  /**
   * Controls the name of the wrapper Java class generated for the .proto file. That class will always contain the .proto file's getDescriptor() method as well as any top-level extensions defined in the .proto file. If java_multiple_files is disabled, then all the other classes from the .proto file will be nested inside the single wrapper outer class.
   */
  @GeneratedProperty(8)
  public val javaOuterClassname: String?
    get() = _javaOuterClassname?.value()

  /**
   * Sets the Go package where structs generated from this .proto will be placed. If omitted, the Go package will be derived from the following:   - The basename of the package import path, if provided.   - Otherwise, the package statement in the .proto file, if present.   - Otherwise, the basename of the .proto file, without extension.
   */
  @GeneratedProperty(11)
  public val goPackage: String?
    get() = _goPackage?.value()

  /**
   * Sets the objective c class prefix which is prepended to all objective c generated classes from this .proto. There is no default.
   */
  @GeneratedProperty(36)
  public val objcClassPrefix: String?
    get() = _objcClassPrefix?.value()

  /**
   * Namespace for generated classes; defaults to the package.
   */
  @GeneratedProperty(37)
  public val csharpNamespace: String?
    get() = _csharpNamespace?.value()

  /**
   * By default Swift generators will take the proto package and CamelCase it replacing '.' with underscore and use that to prefix the types/symbols defined. When this options is provided, they will use this value instead to prefix the types/symbols defined.
   */
  @GeneratedProperty(39)
  public val swiftPrefix: String?
    get() = _swiftPrefix?.value()

  /**
   * Sets the php class prefix which is prepended to all php generated classes from this .proto. Default is empty.
   */
  @GeneratedProperty(40)
  public val phpClassPrefix: String?
    get() = _phpClassPrefix?.value()

  /**
   * Use this option to change the namespace of php generated classes. Default is empty. When this option is empty, the package name will be used for determining the namespace.
   */
  @GeneratedProperty(41)
  public val phpNamespace: String?
    get() = _phpNamespace?.value()

  /**
   * Use this option to change the namespace of php generated metadata classes. Default is empty. When this option is empty, the proto file name will be used for determining the namespace.
   */
  @GeneratedProperty(44)
  public val phpMetadataNamespace: String?
    get() = _phpMetadataNamespace?.value()

  /**
   * Use this option to change the package of ruby generated classes. Default is empty. When this option is not set, the package name will be used for determining the ruby package.
   */
  @GeneratedProperty(45)
  public val rubyPackage: String?
    get() = _rubyPackage?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_javaPackage != null) {
      writer.writeTag(10u).write(_javaPackage.wireValue())
    }
    if (_javaOuterClassname != null) {
      writer.writeTag(66u).write(_javaOuterClassname.wireValue())
    }
    if (optimizeFor != null) {
      writer.writeTag(72u).write(optimizeFor)
    }
    if (javaMultipleFiles != null) {
      writer.writeTag(80u).write(javaMultipleFiles)
    }
    if (_goPackage != null) {
      writer.writeTag(90u).write(_goPackage.wireValue())
    }
    if (ccGenericServices != null) {
      writer.writeTag(128u).write(ccGenericServices)
    }
    if (javaGenericServices != null) {
      writer.writeTag(136u).write(javaGenericServices)
    }
    if (pyGenericServices != null) {
      writer.writeTag(144u).write(pyGenericServices)
    }
    if (javaGenerateEqualsAndHash != null) {
      writer.writeTag(160u).write(javaGenerateEqualsAndHash)
    }
    if (deprecated != null) {
      writer.writeTag(184u).write(deprecated)
    }
    if (javaStringCheckUtf8 != null) {
      writer.writeTag(216u).write(javaStringCheckUtf8)
    }
    if (ccEnableArenas != null) {
      writer.writeTag(248u).write(ccEnableArenas)
    }
    if (_objcClassPrefix != null) {
      writer.writeTag(290u).write(_objcClassPrefix.wireValue())
    }
    if (_csharpNamespace != null) {
      writer.writeTag(298u).write(_csharpNamespace.wireValue())
    }
    if (_swiftPrefix != null) {
      writer.writeTag(314u).write(_swiftPrefix.wireValue())
    }
    if (_phpClassPrefix != null) {
      writer.writeTag(322u).write(_phpClassPrefix.wireValue())
    }
    if (_phpNamespace != null) {
      writer.writeTag(330u).write(_phpNamespace.wireValue())
    }
    if (_phpMetadataNamespace != null) {
      writer.writeTag(354u).write(_phpMetadataNamespace.wireValue())
    }
    if (_rubyPackage != null) {
      writer.writeTag(362u).write(_rubyPackage.wireValue())
    }
    if (features != null) {
      writer.writeTag(402u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FileOptions &&
      other.javaPackage == this.javaPackage &&
      other.javaOuterClassname == this.javaOuterClassname &&
      other.optimizeFor == this.optimizeFor &&
      other.javaMultipleFiles == this.javaMultipleFiles &&
      other.goPackage == this.goPackage &&
      other.ccGenericServices == this.ccGenericServices &&
      other.javaGenericServices == this.javaGenericServices &&
      other.pyGenericServices == this.pyGenericServices &&
      other.javaGenerateEqualsAndHash == this.javaGenerateEqualsAndHash &&
      other.deprecated == this.deprecated &&
      other.javaStringCheckUtf8 == this.javaStringCheckUtf8 &&
      other.ccEnableArenas == this.ccEnableArenas &&
      other.objcClassPrefix == this.objcClassPrefix &&
      other.csharpNamespace == this.csharpNamespace &&
      other.swiftPrefix == this.swiftPrefix &&
      other.phpClassPrefix == this.phpClassPrefix &&
      other.phpNamespace == this.phpNamespace &&
      other.phpMetadataNamespace == this.phpMetadataNamespace &&
      other.rubyPackage == this.rubyPackage &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + javaPackage.hashCode()
    result = 31 * result + javaOuterClassname.hashCode()
    result = 31 * result + optimizeFor.hashCode()
    result = 31 * result + javaMultipleFiles.hashCode()
    result = 31 * result + goPackage.hashCode()
    result = 31 * result + ccGenericServices.hashCode()
    result = 31 * result + javaGenericServices.hashCode()
    result = 31 * result + pyGenericServices.hashCode()
    result = 31 * result + javaGenerateEqualsAndHash.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + javaStringCheckUtf8.hashCode()
    result = 31 * result + ccEnableArenas.hashCode()
    result = 31 * result + objcClassPrefix.hashCode()
    result = 31 * result + csharpNamespace.hashCode()
    result = 31 * result + swiftPrefix.hashCode()
    result = 31 * result + phpClassPrefix.hashCode()
    result = 31 * result + phpNamespace.hashCode()
    result = 31 * result + phpMetadataNamespace.hashCode()
    result = 31 * result + rubyPackage.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "FileOptions(" +
      "javaPackage=$javaPackage, " +
      "javaOuterClassname=$javaOuterClassname, " +
      "optimizeFor=$optimizeFor, " +
      "javaMultipleFiles=$javaMultipleFiles, " +
      "goPackage=$goPackage, " +
      "ccGenericServices=$ccGenericServices, " +
      "javaGenericServices=$javaGenericServices, " +
      "pyGenericServices=$pyGenericServices, " +
      "javaGenerateEqualsAndHash=$javaGenerateEqualsAndHash, " +
      "deprecated=$deprecated, " +
      "javaStringCheckUtf8=$javaStringCheckUtf8, " +
      "ccEnableArenas=$ccEnableArenas, " +
      "objcClassPrefix=$objcClassPrefix, " +
      "csharpNamespace=$csharpNamespace, " +
      "swiftPrefix=$swiftPrefix, " +
      "phpClassPrefix=$phpClassPrefix, " +
      "phpNamespace=$phpNamespace, " +
      "phpMetadataNamespace=$phpMetadataNamespace, " +
      "rubyPackage=$rubyPackage, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FileOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _javaPackageRef: LazyReference<Bytes, String>? = null

    public var javaPackage: String?
      get() = _javaPackageRef?.value()
      set(newValue) {
        _javaPackageRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _javaOuterClassnameRef: LazyReference<Bytes, String>? = null

    public var javaOuterClassname: String?
      get() = _javaOuterClassnameRef?.value()
      set(newValue) {
        _javaOuterClassnameRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var optimizeFor: OptimizeMode? = null

    public var javaMultipleFiles: Boolean? = null

    private var _goPackageRef: LazyReference<Bytes, String>? = null

    public var goPackage: String?
      get() = _goPackageRef?.value()
      set(newValue) {
        _goPackageRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var ccGenericServices: Boolean? = null

    public var javaGenericServices: Boolean? = null

    public var pyGenericServices: Boolean? = null

    @Deprecated("deprecated in proto")
    public var javaGenerateEqualsAndHash: Boolean? = null

    public var deprecated: Boolean? = null

    public var javaStringCheckUtf8: Boolean? = null

    public var ccEnableArenas: Boolean? = null

    private var _objcClassPrefixRef: LazyReference<Bytes, String>? = null

    public var objcClassPrefix: String?
      get() = _objcClassPrefixRef?.value()
      set(newValue) {
        _objcClassPrefixRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _csharpNamespaceRef: LazyReference<Bytes, String>? = null

    public var csharpNamespace: String?
      get() = _csharpNamespaceRef?.value()
      set(newValue) {
        _csharpNamespaceRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _swiftPrefixRef: LazyReference<Bytes, String>? = null

    public var swiftPrefix: String?
      get() = _swiftPrefixRef?.value()
      set(newValue) {
        _swiftPrefixRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _phpClassPrefixRef: LazyReference<Bytes, String>? = null

    public var phpClassPrefix: String?
      get() = _phpClassPrefixRef?.value()
      set(newValue) {
        _phpClassPrefixRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _phpNamespaceRef: LazyReference<Bytes, String>? = null

    public var phpNamespace: String?
      get() = _phpNamespaceRef?.value()
      set(newValue) {
        _phpNamespaceRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _phpMetadataNamespaceRef: LazyReference<Bytes, String>? = null

    public var phpMetadataNamespace: String?
      get() = _phpMetadataNamespaceRef?.value()
      set(newValue) {
        _phpMetadataNamespaceRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    private var _rubyPackageRef: LazyReference<Bytes, String>? = null

    public var rubyPackage: String?
      get() = _rubyPackageRef?.value()
      set(newValue) {
        _rubyPackageRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FileOptions =
      FileOptions(
        _javaPackageRef,
        _javaOuterClassnameRef,
        optimizeFor,
        javaMultipleFiles,
        _goPackageRef,
        ccGenericServices,
        javaGenericServices,
        pyGenericServices,
        javaGenerateEqualsAndHash,
        deprecated,
        javaStringCheckUtf8,
        ccEnableArenas,
        _objcClassPrefixRef,
        _csharpNamespaceRef,
        _swiftPrefixRef,
        _phpClassPrefixRef,
        _phpNamespaceRef,
        _phpMetadataNamespaceRef,
        _rubyPackageRef,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FileOptions): Builder =
        Builder().also {
          it._javaPackageRef = msg._javaPackage
          it._javaOuterClassnameRef = msg._javaOuterClassname
          it.optimizeFor = msg.optimizeFor
          it.javaMultipleFiles = msg.javaMultipleFiles
          it._goPackageRef = msg._goPackage
          it.ccGenericServices = msg.ccGenericServices
          it.javaGenericServices = msg.javaGenericServices
          it.pyGenericServices = msg.pyGenericServices
          it.javaGenerateEqualsAndHash = msg.javaGenerateEqualsAndHash
          it.deprecated = msg.deprecated
          it.javaStringCheckUtf8 = msg.javaStringCheckUtf8
          it.ccEnableArenas = msg.ccEnableArenas
          it._objcClassPrefixRef = msg._objcClassPrefix
          it._csharpNamespaceRef = msg._csharpNamespace
          it._swiftPrefixRef = msg._swiftPrefix
          it._phpClassPrefixRef = msg._phpClassPrefix
          it._phpNamespaceRef = msg._phpNamespace
          it._phpMetadataNamespaceRef = msg._phpMetadataNamespace
          it._rubyPackageRef = msg._rubyPackage
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FileOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FileOptions {
      var javaPackage: Bytes? = null
      var javaOuterClassname: Bytes? = null
      var optimizeFor: OptimizeMode? = null
      var javaMultipleFiles: Boolean? = null
      var goPackage: Bytes? = null
      var ccGenericServices: Boolean? = null
      var javaGenericServices: Boolean? = null
      var pyGenericServices: Boolean? = null
      var javaGenerateEqualsAndHash: Boolean? = null
      var deprecated: Boolean? = null
      var javaStringCheckUtf8: Boolean? = null
      var ccEnableArenas: Boolean? = null
      var objcClassPrefix: Bytes? = null
      var csharpNamespace: Bytes? = null
      var swiftPrefix: Bytes? = null
      var phpClassPrefix: Bytes? = null
      var phpNamespace: Bytes? = null
      var phpMetadataNamespace: Bytes? = null
      var rubyPackage: Bytes? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FileOptions(
              javaPackage?.let { LazyReference(it, StringConverter) },
              javaOuterClassname?.let { LazyReference(it, StringConverter) },
              optimizeFor,
              javaMultipleFiles,
              goPackage?.let { LazyReference(it, StringConverter) },
              ccGenericServices,
              javaGenericServices,
              pyGenericServices,
              javaGenerateEqualsAndHash,
              deprecated,
              javaStringCheckUtf8,
              ccEnableArenas,
              objcClassPrefix?.let { LazyReference(it, StringConverter) },
              csharpNamespace?.let { LazyReference(it, StringConverter) },
              swiftPrefix?.let { LazyReference(it, StringConverter) },
              phpClassPrefix?.let { LazyReference(it, StringConverter) },
              phpNamespace?.let { LazyReference(it, StringConverter) },
              phpMetadataNamespace?.let { LazyReference(it, StringConverter) },
              rubyPackage?.let { LazyReference(it, StringConverter) },
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            javaPackage = StringConverter.readValidatedBytes(reader)
          }

          66u -> {
            javaOuterClassname = StringConverter.readValidatedBytes(reader)
          }

          72u -> {
            optimizeFor = reader.readEnum(OptimizeMode)
          }

          80u -> {
            javaMultipleFiles = reader.readBool()
          }

          90u -> {
            goPackage = StringConverter.readValidatedBytes(reader)
          }

          128u -> {
            ccGenericServices = reader.readBool()
          }

          136u -> {
            javaGenericServices = reader.readBool()
          }

          144u -> {
            pyGenericServices = reader.readBool()
          }

          160u -> {
            javaGenerateEqualsAndHash = reader.readBool()
          }

          184u -> {
            deprecated = reader.readBool()
          }

          216u -> {
            javaStringCheckUtf8 = reader.readBool()
          }

          248u -> {
            ccEnableArenas = reader.readBool()
          }

          290u -> {
            objcClassPrefix = StringConverter.readValidatedBytes(reader)
          }

          298u -> {
            csharpNamespace = StringConverter.readValidatedBytes(reader)
          }

          314u -> {
            swiftPrefix = StringConverter.readValidatedBytes(reader)
          }

          322u -> {
            phpClassPrefix = StringConverter.readValidatedBytes(reader)
          }

          330u -> {
            phpNamespace = StringConverter.readValidatedBytes(reader)
          }

          354u -> {
            phpMetadataNamespace = StringConverter.readValidatedBytes(reader)
          }

          362u -> {
            rubyPackage = StringConverter.readValidatedBytes(reader)
          }

          402u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

  /**
   * Generated classes can be optimized for speed or code size.
   */
  public sealed class OptimizeMode(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object SPEED : OptimizeMode(1, "SPEED")

    /**
     * etc.
     */
    public object CODE_SIZE : OptimizeMode(2, "CODE_SIZE")

    public object LITE_RUNTIME : OptimizeMode(3, "LITE_RUNTIME")

    public class UNRECOGNIZED(
      `value`: Int
    ) : OptimizeMode(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<OptimizeMode> {
      override fun deserialize(`value`: Int): OptimizeMode =
        when (value) {
          1 -> SPEED
          2 -> CODE_SIZE
          3 -> LITE_RUNTIME
          else -> UNRECOGNIZED(value)
        }
    }
  }
}

@GeneratedMessage("google.protobuf.MessageOptions")
public class MessageOptions private constructor(
  /**
   * Set true to use the old proto1 MessageSet wire format for extensions. This is provided for backwards-compatibility with the MessageSet wire format.  You should not use this for any other reason:  It's less efficient, has fewer features, and is more complicated.
   *
   *  The message must be defined exactly as follows:   message Foo {     option message_set_wire_format = true;     extensions 4 to max;   } Note that the message cannot have any defined fields; MessageSets only have extensions.
   *
   *  All extensions of your type must be singular messages; e.g. they cannot be int32s, enums, or repeated messages.
   *
   *  Because this is an option, the above two restrictions are not enforced by the protocol compiler.
   */
  @GeneratedProperty(1)
  public val messageSetWireFormat: Boolean?,
  /**
   * Disables the generation of the standard "descriptor()" accessor, which can conflict with a field of the same name.  This is meant to make migration from proto1 easier; new code should avoid fields named "descriptor".
   */
  @GeneratedProperty(2)
  public val noStandardDescriptorAccessor: Boolean?,
  /**
   * Is this message deprecated? Depending on the target platform, this can emit Deprecated annotations for the message, or it will be completely ignored; in the very least, this is a formalization for deprecating messages.
   */
  @GeneratedProperty(3)
  public val deprecated: Boolean?,
  /**
   * Whether the message is an automatically generated map entry type for the maps field.
   *
   *  For maps fields:     map<KeyType, ValueType> map_field = 1; The parsed descriptor looks like:     message MapFieldEntry {         option map_entry = true;         optional KeyType key = 1;         optional ValueType value = 2;     }     repeated MapFieldEntry map_field = 1;
   *
   *  Implementations may choose not to generate the map_entry=true message, but use a native map in the target language to hold the keys and values. The reflection APIs in such implementations still need to work as if the field is a repeated message field.
   *
   *  NOTE: Do not set the option in .proto files. Always use the maps syntax instead. The option should only be implicitly set by the proto compiler parser.
   */
  @GeneratedProperty(7)
  public val mapEntry: Boolean?,
  /**
   * Enable the legacy handling of JSON field name conflicts.  This lowercases and strips underscored from the fields before comparison in proto3 only. The new behavior takes `json_name` into account and applies to proto2 as well.
   *
   *  This should only be used as a temporary measure against broken builds due to the change in behavior for JSON field name conflicts.
   *
   *  TODO This is legacy behavior we plan to remove once downstream teams have had time to migrate.
   */
  @GeneratedProperty(11)
  @Deprecated("deprecated in proto")
  public val deprecatedLegacyJsonFieldConflicts: Boolean?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(12)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (messageSetWireFormat != null) {
      result += sizeOf(8u) + 1
    }
    if (noStandardDescriptorAccessor != null) {
      result += sizeOf(16u) + 1
    }
    if (deprecated != null) {
      result += sizeOf(24u) + 1
    }
    if (mapEntry != null) {
      result += sizeOf(56u) + 1
    }
    if (deprecatedLegacyJsonFieldConflicts != null) {
      result += sizeOf(88u) + 1
    }
    if (features != null) {
      result += sizeOf(98u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (messageSetWireFormat != null) {
      writer.writeTag(8u).write(messageSetWireFormat)
    }
    if (noStandardDescriptorAccessor != null) {
      writer.writeTag(16u).write(noStandardDescriptorAccessor)
    }
    if (deprecated != null) {
      writer.writeTag(24u).write(deprecated)
    }
    if (mapEntry != null) {
      writer.writeTag(56u).write(mapEntry)
    }
    if (deprecatedLegacyJsonFieldConflicts != null) {
      writer.writeTag(88u).write(deprecatedLegacyJsonFieldConflicts)
    }
    if (features != null) {
      writer.writeTag(98u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is MessageOptions &&
      other.messageSetWireFormat == this.messageSetWireFormat &&
      other.noStandardDescriptorAccessor == this.noStandardDescriptorAccessor &&
      other.deprecated == this.deprecated &&
      other.mapEntry == this.mapEntry &&
      other.deprecatedLegacyJsonFieldConflicts == this.deprecatedLegacyJsonFieldConflicts &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + messageSetWireFormat.hashCode()
    result = 31 * result + noStandardDescriptorAccessor.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + mapEntry.hashCode()
    result = 31 * result + deprecatedLegacyJsonFieldConflicts.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "MessageOptions(" +
      "messageSetWireFormat=$messageSetWireFormat, " +
      "noStandardDescriptorAccessor=$noStandardDescriptorAccessor, " +
      "deprecated=$deprecated, " +
      "mapEntry=$mapEntry, " +
      "deprecatedLegacyJsonFieldConflicts=$deprecatedLegacyJsonFieldConflicts, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): MessageOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var messageSetWireFormat: Boolean? = null

    public var noStandardDescriptorAccessor: Boolean? = null

    public var deprecated: Boolean? = null

    public var mapEntry: Boolean? = null

    @Deprecated("deprecated in proto")
    public var deprecatedLegacyJsonFieldConflicts: Boolean? = null

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): MessageOptions =
      MessageOptions(
        messageSetWireFormat,
        noStandardDescriptorAccessor,
        deprecated,
        mapEntry,
        deprecatedLegacyJsonFieldConflicts,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: MessageOptions): Builder =
        Builder().also {
          it.messageSetWireFormat = msg.messageSetWireFormat
          it.noStandardDescriptorAccessor = msg.noStandardDescriptorAccessor
          it.deprecated = msg.deprecated
          it.mapEntry = msg.mapEntry
          it.deprecatedLegacyJsonFieldConflicts = msg.deprecatedLegacyJsonFieldConflicts
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<MessageOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): MessageOptions {
      var messageSetWireFormat: Boolean? = null
      var noStandardDescriptorAccessor: Boolean? = null
      var deprecated: Boolean? = null
      var mapEntry: Boolean? = null
      var deprecatedLegacyJsonFieldConflicts: Boolean? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return MessageOptions(
              messageSetWireFormat,
              noStandardDescriptorAccessor,
              deprecated,
              mapEntry,
              deprecatedLegacyJsonFieldConflicts,
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            messageSetWireFormat = reader.readBool()
          }

          16u -> {
            noStandardDescriptorAccessor = reader.readBool()
          }

          24u -> {
            deprecated = reader.readBool()
          }

          56u -> {
            mapEntry = reader.readBool()
          }

          88u -> {
            deprecatedLegacyJsonFieldConflicts = reader.readBool()
          }

          98u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

@GeneratedMessage("google.protobuf.FieldOptions")
public class FieldOptions private constructor(
  /**
   * NOTE: ctype is deprecated. Use `features.(pb.cpp).string_type` instead. The ctype option instructs the C++ code generator to use a different representation of the field than it normally would.  See the specific options below.  This option is only implemented to support use of [ctype=CORD] and [ctype=STRING] (the default) on non-repeated fields of type "bytes" in the open source release. TODO: make ctype actually deprecated.
   */
  @GeneratedProperty(1)
  public val ctype: CType?,
  /**
   * The packed option can be enabled for repeated primitive fields to enable a more efficient representation on the wire. Rather than repeatedly writing the tag and type for each element, the entire array is encoded as a single length-delimited blob. In proto3, only explicit setting it to false will avoid using packed encoding.  This option is prohibited in Editions, but the `repeated_field_encoding` feature can be used to control the behavior.
   */
  @GeneratedProperty(2)
  public val packed: Boolean?,
  /**
   * Is this field deprecated? Depending on the target platform, this can emit Deprecated annotations for accessors, or it will be completely ignored; in the very least, this is a formalization for deprecating fields.
   */
  @GeneratedProperty(3)
  public val deprecated: Boolean?,
  /**
   * Should this field be parsed lazily?  Lazy applies only to message-type fields.  It means that when the outer message is initially parsed, the inner message's contents will not be parsed but instead stored in encoded form.  The inner message will actually be parsed when it is first accessed.
   *
   *  This is only a hint.  Implementations are free to choose whether to use eager or lazy parsing regardless of the value of this option.  However, setting this option true suggests that the protocol author believes that using lazy parsing on this field is worth the additional bookkeeping overhead typically needed to implement it.
   *
   *  This option does not affect the public interface of any generated code; all method signatures remain the same.  Furthermore, thread-safety of the interface is not affected by this option; const methods remain safe to call from multiple threads concurrently, while non-const methods continue to require exclusive access.
   *
   *  Note that lazy message fields are still eagerly verified to check ill-formed wireformat or missing required fields. Calling IsInitialized() on the outer message would fail if the inner message has missing required fields. Failed verification would result in parsing failure (except when uninitialized messages are acceptable).
   */
  @GeneratedProperty(5)
  public val lazy: Boolean?,
  /**
   * The jstype option determines the JavaScript type used for values of the field.  The option is permitted only for 64 bit integral and fixed types (int64, uint64, sint64, fixed64, sfixed64).  A field with jstype JS_STRING is represented as JavaScript string, which avoids loss of precision that can happen when a large value is converted to a floating point JavaScript. Specifying JS_NUMBER for the jstype causes the generated JavaScript code to use the JavaScript "number" type.  The behavior of the default option JS_NORMAL is implementation dependent.
   *
   *  This option is an enum to permit additional types to be added, e.g. goog.math.Integer.
   */
  @GeneratedProperty(6)
  public val jstype: JSType?,
  /**
   * For Google-internal migration only. Do not use.
   */
  @GeneratedProperty(10)
  public val weak: Boolean?,
  /**
   * unverified_lazy does no correctness checks on the byte stream. This should only be used where lazy with verification is prohibitive for performance reasons.
   */
  @GeneratedProperty(15)
  public val unverifiedLazy: Boolean?,
  /**
   * Indicate that the field value should not be printed out when using debug formats, e.g. when the field contains sensitive credentials.
   */
  @GeneratedProperty(16)
  public val debugRedact: Boolean?,
  @GeneratedProperty(17)
  public val retention: OptionRetention?,
  @GeneratedProperty(19)
  public val targets: List<OptionTargetType>,
  @GeneratedProperty(20)
  public val editionDefaults: List<EditionDefault>,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(21)
  public val features: FeatureSet?,
  @GeneratedProperty(22)
  public val featureSupport: FeatureSupport?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (ctype != null) {
      result += sizeOf(8u) + sizeOf(ctype)
    }
    if (packed != null) {
      result += sizeOf(16u) + 1
    }
    if (deprecated != null) {
      result += sizeOf(24u) + 1
    }
    if (lazy != null) {
      result += sizeOf(40u) + 1
    }
    if (jstype != null) {
      result += sizeOf(48u) + sizeOf(jstype)
    }
    if (weak != null) {
      result += sizeOf(80u) + 1
    }
    if (unverifiedLazy != null) {
      result += sizeOf(120u) + 1
    }
    if (debugRedact != null) {
      result += sizeOf(128u) + 1
    }
    if (retention != null) {
      result += sizeOf(136u) + sizeOf(retention)
    }
    if (targets.isNotEmpty()) {
      result += (sizeOf(152u) * targets.size) + targets.sumOf { sizeOf(it) }
    }
    if (editionDefaults.isNotEmpty()) {
      result += (sizeOf(162u) * editionDefaults.size) + editionDefaults.sumOf { sizeOf(it) }
    }
    if (features != null) {
      result += sizeOf(170u) + sizeOf(features)
    }
    if (featureSupport != null) {
      result += sizeOf(178u) + sizeOf(featureSupport)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (ctype != null) {
      writer.writeTag(8u).write(ctype)
    }
    if (packed != null) {
      writer.writeTag(16u).write(packed)
    }
    if (deprecated != null) {
      writer.writeTag(24u).write(deprecated)
    }
    if (lazy != null) {
      writer.writeTag(40u).write(lazy)
    }
    if (jstype != null) {
      writer.writeTag(48u).write(jstype)
    }
    if (weak != null) {
      writer.writeTag(80u).write(weak)
    }
    if (unverifiedLazy != null) {
      writer.writeTag(120u).write(unverifiedLazy)
    }
    if (debugRedact != null) {
      writer.writeTag(128u).write(debugRedact)
    }
    if (retention != null) {
      writer.writeTag(136u).write(retention)
    }
    targets.forEach { writer.writeTag(152u).write(it) }
    editionDefaults.forEach { writer.writeTag(162u).write(it) }
    if (features != null) {
      writer.writeTag(170u).write(features)
    }
    if (featureSupport != null) {
      writer.writeTag(178u).write(featureSupport)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FieldOptions &&
      other.ctype == this.ctype &&
      other.packed == this.packed &&
      other.deprecated == this.deprecated &&
      other.lazy == this.lazy &&
      other.jstype == this.jstype &&
      other.weak == this.weak &&
      other.unverifiedLazy == this.unverifiedLazy &&
      other.debugRedact == this.debugRedact &&
      other.retention == this.retention &&
      other.targets == this.targets &&
      other.editionDefaults == this.editionDefaults &&
      other.features == this.features &&
      other.featureSupport == this.featureSupport &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + ctype.hashCode()
    result = 31 * result + packed.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + lazy.hashCode()
    result = 31 * result + jstype.hashCode()
    result = 31 * result + weak.hashCode()
    result = 31 * result + unverifiedLazy.hashCode()
    result = 31 * result + debugRedact.hashCode()
    result = 31 * result + retention.hashCode()
    result = 31 * result + targets.hashCode()
    result = 31 * result + editionDefaults.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + featureSupport.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "FieldOptions(" +
      "ctype=$ctype, " +
      "packed=$packed, " +
      "deprecated=$deprecated, " +
      "lazy=$lazy, " +
      "jstype=$jstype, " +
      "weak=$weak, " +
      "unverifiedLazy=$unverifiedLazy, " +
      "debugRedact=$debugRedact, " +
      "retention=$retention, " +
      "targets=$targets, " +
      "editionDefaults=$editionDefaults, " +
      "features=$features, " +
      "featureSupport=$featureSupport, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FieldOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var ctype: CType? = null

    public var packed: Boolean? = null

    public var deprecated: Boolean? = null

    public var lazy: Boolean? = null

    public var jstype: JSType? = null

    public var weak: Boolean? = null

    public var unverifiedLazy: Boolean? = null

    public var debugRedact: Boolean? = null

    public var retention: OptionRetention? = null

    public var targets: List<OptionTargetType> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var editionDefaults: List<EditionDefault> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var features: FeatureSet? = null

    public var featureSupport: FeatureSupport? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FieldOptions =
      FieldOptions(
        ctype,
        packed,
        deprecated,
        lazy,
        jstype,
        weak,
        unverifiedLazy,
        debugRedact,
        retention,
        freezeList(targets),
        freezeList(editionDefaults),
        features,
        featureSupport,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FieldOptions): Builder =
        Builder().also {
          it.ctype = msg.ctype
          it.packed = msg.packed
          it.deprecated = msg.deprecated
          it.lazy = msg.lazy
          it.jstype = msg.jstype
          it.weak = msg.weak
          it.unverifiedLazy = msg.unverifiedLazy
          it.debugRedact = msg.debugRedact
          it.retention = msg.retention
          it.targets = msg.targets
          it.editionDefaults = msg.editionDefaults
          it.features = msg.features
          it.featureSupport = msg.featureSupport
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FieldOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FieldOptions {
      var ctype: CType? = null
      var packed: Boolean? = null
      var deprecated: Boolean? = null
      var lazy: Boolean? = null
      var jstype: JSType? = null
      var weak: Boolean? = null
      var unverifiedLazy: Boolean? = null
      var debugRedact: Boolean? = null
      var retention: OptionRetention? = null
      var targets: ListBuilder<OptionTargetType>? = null
      var editionDefaults: ListBuilder<EditionDefault>? = null
      var features: FeatureSet? = null
      var featureSupport: FeatureSupport? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FieldOptions(
              ctype,
              packed,
              deprecated,
              lazy,
              jstype,
              weak,
              unverifiedLazy,
              debugRedact,
              retention,
              targets?.build() ?: emptyList(),
              editionDefaults?.build() ?: emptyList(),
              features,
              featureSupport,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            ctype = reader.readEnum(CType)
          }

          16u -> {
            packed = reader.readBool()
          }

          24u -> {
            deprecated = reader.readBool()
          }

          40u -> {
            lazy = reader.readBool()
          }

          48u -> {
            jstype = reader.readEnum(JSType)
          }

          80u -> {
            weak = reader.readBool()
          }

          120u -> {
            unverifiedLazy = reader.readBool()
          }

          128u -> {
            debugRedact = reader.readBool()
          }

          136u -> {
            retention = reader.readEnum(OptionRetention)
          }

          152u -> {
            targets =
              (targets ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readEnum(OptionTargetType))
                }
              }
          }

          154u -> {
            targets =
              (targets ?: listBuilder()).apply {
                reader.readRepeated(true) {
                  add(reader.readEnum(OptionTargetType))
                }
              }
          }

          162u -> {
            editionDefaults =
              (editionDefaults ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(EditionDefault))
                }
              }
          }

          170u -> {
            features = reader.readMessage(FeatureSet)
          }

          178u -> {
            featureSupport = reader.readMessage(FeatureSupport)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

  public sealed class CType(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    /**
     * Default mode.
     */
    public object STRING : CType(0, "STRING")

    /**
     * The option [ctype=CORD] may be applied to a non-repeated field of type "bytes". It indicates that in C++, the data should be stored in a Cord instead of a string.  For very large strings, this may reduce memory fragmentation. It may also allow better performance when parsing from a Cord, or when parsing with aliasing enabled, as the parsed Cord may then alias the original buffer.
     */
    public object CORD : CType(1, "CORD")

    public object STRING_PIECE : CType(2, "STRING_PIECE")

    public class UNRECOGNIZED(
      `value`: Int
    ) : CType(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<CType> {
      override fun deserialize(`value`: Int): CType =
        when (value) {
          0 -> STRING
          1 -> CORD
          2 -> STRING_PIECE
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class JSType(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    /**
     * Use the default type.
     */
    public object JS_NORMAL : JSType(0, "JS_NORMAL")

    /**
     * Use JavaScript strings.
     */
    public object JS_STRING : JSType(1, "JS_STRING")

    /**
     * Use JavaScript numbers.
     */
    public object JS_NUMBER : JSType(2, "JS_NUMBER")

    public class UNRECOGNIZED(
      `value`: Int
    ) : JSType(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<JSType> {
      override fun deserialize(`value`: Int): JSType =
        when (value) {
          0 -> JS_NORMAL
          1 -> JS_STRING
          2 -> JS_NUMBER
          else -> UNRECOGNIZED(value)
        }
    }
  }

  /**
   * If set to RETENTION_SOURCE, the option will be omitted from the binary.
   */
  public sealed class OptionRetention(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object RETENTION_UNKNOWN : OptionRetention(0, "RETENTION_UNKNOWN")

    public object RETENTION_RUNTIME : OptionRetention(1, "RETENTION_RUNTIME")

    public object RETENTION_SOURCE : OptionRetention(2, "RETENTION_SOURCE")

    public class UNRECOGNIZED(
      `value`: Int
    ) : OptionRetention(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<OptionRetention> {
      override fun deserialize(`value`: Int): OptionRetention =
        when (value) {
          0 -> RETENTION_UNKNOWN
          1 -> RETENTION_RUNTIME
          2 -> RETENTION_SOURCE
          else -> UNRECOGNIZED(value)
        }
    }
  }

  /**
   * This indicates the types of entities that the field may apply to when used as an option. If it is unset, then the field may be freely used as an option on any kind of entity.
   */
  public sealed class OptionTargetType(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object TARGET_TYPE_UNKNOWN : OptionTargetType(0, "TARGET_TYPE_UNKNOWN")

    public object TARGET_TYPE_FILE : OptionTargetType(1, "TARGET_TYPE_FILE")

    public object TARGET_TYPE_EXTENSION_RANGE : OptionTargetType(2, "TARGET_TYPE_EXTENSION_RANGE")

    public object TARGET_TYPE_MESSAGE : OptionTargetType(3, "TARGET_TYPE_MESSAGE")

    public object TARGET_TYPE_FIELD : OptionTargetType(4, "TARGET_TYPE_FIELD")

    public object TARGET_TYPE_ONEOF : OptionTargetType(5, "TARGET_TYPE_ONEOF")

    public object TARGET_TYPE_ENUM : OptionTargetType(6, "TARGET_TYPE_ENUM")

    public object TARGET_TYPE_ENUM_ENTRY : OptionTargetType(7, "TARGET_TYPE_ENUM_ENTRY")

    public object TARGET_TYPE_SERVICE : OptionTargetType(8, "TARGET_TYPE_SERVICE")

    public object TARGET_TYPE_METHOD : OptionTargetType(9, "TARGET_TYPE_METHOD")

    public class UNRECOGNIZED(
      `value`: Int
    ) : OptionTargetType(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<OptionTargetType> {
      override fun deserialize(`value`: Int): OptionTargetType =
        when (value) {
          0 -> TARGET_TYPE_UNKNOWN
          1 -> TARGET_TYPE_FILE
          2 -> TARGET_TYPE_EXTENSION_RANGE
          3 -> TARGET_TYPE_MESSAGE
          4 -> TARGET_TYPE_FIELD
          5 -> TARGET_TYPE_ONEOF
          6 -> TARGET_TYPE_ENUM
          7 -> TARGET_TYPE_ENUM_ENTRY
          8 -> TARGET_TYPE_SERVICE
          9 -> TARGET_TYPE_METHOD
          else -> UNRECOGNIZED(value)
        }
    }
  }

  @GeneratedMessage("google.protobuf.FieldOptions.EditionDefault")
  public class EditionDefault private constructor(
    private val _value: LazyReference<Bytes, String>?,
    @GeneratedProperty(3)
    public val edition: Edition?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (_value != null) {
        result += sizeOf(18u) + sizeOf(_value.wireValue())
      }
      if (edition != null) {
        result += sizeOf(24u) + sizeOf(edition)
      }
      result += unknownFields.size()
      result
    }

    @GeneratedProperty(2)
    public val `value`: String?
      get() = _value?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (_value != null) {
        writer.writeTag(18u).write(_value.wireValue())
      }
      if (edition != null) {
        writer.writeTag(24u).write(edition)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is EditionDefault &&
        other.`value` == this.`value` &&
        other.edition == this.edition &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + `value`.hashCode()
      result = 31 * result + edition.hashCode()
      return result
    }

    override fun toString(): String =
      "EditionDefault(" +
        "`value`=$`value`, " +
        "edition=$edition" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): EditionDefault =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      private var _valueRef: LazyReference<Bytes, String>? = null

      public var `value`: String?
        get() = _valueRef?.value()
        set(newValue) {
          _valueRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var edition: Edition? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): EditionDefault =
        EditionDefault(
          _valueRef,
          edition,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: EditionDefault): Builder =
          Builder().also {
            it._valueRef = msg._value
            it.edition = msg.edition
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<EditionDefault>() {
      @JvmStatic
      override fun deserialize(reader: Reader): EditionDefault {
        var `value`: Bytes? = null
        var edition: Edition? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return EditionDefault(
                `value`?.let { LazyReference(it, StringConverter) },
                edition,
                UnknownFieldSet.from(unknownFields)
              )
            }

            18u -> {
              `value` = StringConverter.readValidatedBytes(reader)
            }

            24u -> {
              edition = reader.readEnum(Edition)
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
      public operator fun invoke(dsl: Builder.() -> Unit): EditionDefault =
        Builder().apply(dsl).build()
    }
  }

  /**
   * Information about the support window of a feature.
   */
  @GeneratedMessage("google.protobuf.FieldOptions.FeatureSupport")
  public class FeatureSupport private constructor(
    /**
     * The edition that this feature was first available in.  In editions earlier than this one, the default assigned to EDITION_LEGACY will be used, and proto files will not be able to override it.
     */
    @GeneratedProperty(1)
    public val editionIntroduced: Edition?,
    /**
     * The edition this feature becomes deprecated in.  Using this after this edition may trigger warnings.
     */
    @GeneratedProperty(2)
    public val editionDeprecated: Edition?,
    private val _deprecationWarning: LazyReference<Bytes, String>?,
    /**
     * The edition this feature is no longer available in.  In editions after this one, the last default assigned will be used, and proto files will not be able to override it.
     */
    @GeneratedProperty(4)
    public val editionRemoved: Edition?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (editionIntroduced != null) {
        result += sizeOf(8u) + sizeOf(editionIntroduced)
      }
      if (editionDeprecated != null) {
        result += sizeOf(16u) + sizeOf(editionDeprecated)
      }
      if (_deprecationWarning != null) {
        result += sizeOf(26u) + sizeOf(_deprecationWarning.wireValue())
      }
      if (editionRemoved != null) {
        result += sizeOf(32u) + sizeOf(editionRemoved)
      }
      result += unknownFields.size()
      result
    }

    /**
     * The deprecation warning text if this feature is used after the edition it was marked deprecated in.
     */
    @GeneratedProperty(3)
    public val deprecationWarning: String?
      get() = _deprecationWarning?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (editionIntroduced != null) {
        writer.writeTag(8u).write(editionIntroduced)
      }
      if (editionDeprecated != null) {
        writer.writeTag(16u).write(editionDeprecated)
      }
      if (_deprecationWarning != null) {
        writer.writeTag(26u).write(_deprecationWarning.wireValue())
      }
      if (editionRemoved != null) {
        writer.writeTag(32u).write(editionRemoved)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is FeatureSupport &&
        other.editionIntroduced == this.editionIntroduced &&
        other.editionDeprecated == this.editionDeprecated &&
        other.deprecationWarning == this.deprecationWarning &&
        other.editionRemoved == this.editionRemoved &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + editionIntroduced.hashCode()
      result = 31 * result + editionDeprecated.hashCode()
      result = 31 * result + deprecationWarning.hashCode()
      result = 31 * result + editionRemoved.hashCode()
      return result
    }

    override fun toString(): String =
      "FeatureSupport(" +
        "editionIntroduced=$editionIntroduced, " +
        "editionDeprecated=$editionDeprecated, " +
        "deprecationWarning=$deprecationWarning, " +
        "editionRemoved=$editionRemoved" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): FeatureSupport =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var editionIntroduced: Edition? = null

      public var editionDeprecated: Edition? = null

      private var _deprecationWarningRef: LazyReference<Bytes, String>? = null

      public var deprecationWarning: String?
        get() = _deprecationWarningRef?.value()
        set(newValue) {
          _deprecationWarningRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var editionRemoved: Edition? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): FeatureSupport =
        FeatureSupport(
          editionIntroduced,
          editionDeprecated,
          _deprecationWarningRef,
          editionRemoved,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: FeatureSupport): Builder =
          Builder().also {
            it.editionIntroduced = msg.editionIntroduced
            it.editionDeprecated = msg.editionDeprecated
            it._deprecationWarningRef = msg._deprecationWarning
            it.editionRemoved = msg.editionRemoved
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<FeatureSupport>() {
      @JvmStatic
      override fun deserialize(reader: Reader): FeatureSupport {
        var editionIntroduced: Edition? = null
        var editionDeprecated: Edition? = null
        var deprecationWarning: Bytes? = null
        var editionRemoved: Edition? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return FeatureSupport(
                editionIntroduced,
                editionDeprecated,
                deprecationWarning?.let { LazyReference(it, StringConverter) },
                editionRemoved,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              editionIntroduced = reader.readEnum(Edition)
            }

            16u -> {
              editionDeprecated = reader.readEnum(Edition)
            }

            26u -> {
              deprecationWarning = StringConverter.readValidatedBytes(reader)
            }

            32u -> {
              editionRemoved = reader.readEnum(Edition)
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
      public operator fun invoke(dsl: Builder.() -> Unit): FeatureSupport =
        Builder().apply(dsl).build()
    }
  }
}

@GeneratedMessage("google.protobuf.OneofOptions")
public class OneofOptions private constructor(
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(1)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (features != null) {
      result += sizeOf(10u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (features != null) {
      writer.writeTag(10u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is OneofOptions &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "OneofOptions(" +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): OneofOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): OneofOptions =
      OneofOptions(
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: OneofOptions): Builder =
        Builder().also {
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<OneofOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): OneofOptions {
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return OneofOptions(
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

@GeneratedMessage("google.protobuf.EnumOptions")
public class EnumOptions private constructor(
  /**
   * Set this option to true to allow mapping different tag names to the same value.
   */
  @GeneratedProperty(2)
  public val allowAlias: Boolean?,
  /**
   * Is this enum deprecated? Depending on the target platform, this can emit Deprecated annotations for the enum, or it will be completely ignored; in the very least, this is a formalization for deprecating enums.
   */
  @GeneratedProperty(3)
  public val deprecated: Boolean?,
  /**
   * Enable the legacy handling of JSON field name conflicts.  This lowercases and strips underscored from the fields before comparison in proto3 only. The new behavior takes `json_name` into account and applies to proto2 as well. TODO Remove this legacy behavior once downstream teams have had time to migrate.
   */
  @GeneratedProperty(6)
  @Deprecated("deprecated in proto")
  public val deprecatedLegacyJsonFieldConflicts: Boolean?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(7)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (allowAlias != null) {
      result += sizeOf(16u) + 1
    }
    if (deprecated != null) {
      result += sizeOf(24u) + 1
    }
    if (deprecatedLegacyJsonFieldConflicts != null) {
      result += sizeOf(48u) + 1
    }
    if (features != null) {
      result += sizeOf(58u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (allowAlias != null) {
      writer.writeTag(16u).write(allowAlias)
    }
    if (deprecated != null) {
      writer.writeTag(24u).write(deprecated)
    }
    if (deprecatedLegacyJsonFieldConflicts != null) {
      writer.writeTag(48u).write(deprecatedLegacyJsonFieldConflicts)
    }
    if (features != null) {
      writer.writeTag(58u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumOptions &&
      other.allowAlias == this.allowAlias &&
      other.deprecated == this.deprecated &&
      other.deprecatedLegacyJsonFieldConflicts == this.deprecatedLegacyJsonFieldConflicts &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + allowAlias.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + deprecatedLegacyJsonFieldConflicts.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumOptions(" +
      "allowAlias=$allowAlias, " +
      "deprecated=$deprecated, " +
      "deprecatedLegacyJsonFieldConflicts=$deprecatedLegacyJsonFieldConflicts, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var allowAlias: Boolean? = null

    public var deprecated: Boolean? = null

    @Deprecated("deprecated in proto")
    public var deprecatedLegacyJsonFieldConflicts: Boolean? = null

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumOptions =
      EnumOptions(
        allowAlias,
        deprecated,
        deprecatedLegacyJsonFieldConflicts,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumOptions): Builder =
        Builder().also {
          it.allowAlias = msg.allowAlias
          it.deprecated = msg.deprecated
          it.deprecatedLegacyJsonFieldConflicts = msg.deprecatedLegacyJsonFieldConflicts
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumOptions {
      var allowAlias: Boolean? = null
      var deprecated: Boolean? = null
      var deprecatedLegacyJsonFieldConflicts: Boolean? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumOptions(
              allowAlias,
              deprecated,
              deprecatedLegacyJsonFieldConflicts,
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          16u -> {
            allowAlias = reader.readBool()
          }

          24u -> {
            deprecated = reader.readBool()
          }

          48u -> {
            deprecatedLegacyJsonFieldConflicts = reader.readBool()
          }

          58u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

@GeneratedMessage("google.protobuf.EnumValueOptions")
public class EnumValueOptions private constructor(
  /**
   * Is this enum value deprecated? Depending on the target platform, this can emit Deprecated annotations for the enum value, or it will be completely ignored; in the very least, this is a formalization for deprecating enum values.
   */
  @GeneratedProperty(1)
  public val deprecated: Boolean?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(2)
  public val features: FeatureSet?,
  /**
   * Indicate that fields annotated with this enum value should not be printed out when using debug formats, e.g. when the field contains sensitive credentials.
   */
  @GeneratedProperty(3)
  public val debugRedact: Boolean?,
  /**
   * Information about the support window of a feature value.
   */
  @GeneratedProperty(4)
  public val featureSupport: FieldOptions.FeatureSupport?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (deprecated != null) {
      result += sizeOf(8u) + 1
    }
    if (features != null) {
      result += sizeOf(18u) + sizeOf(features)
    }
    if (debugRedact != null) {
      result += sizeOf(24u) + 1
    }
    if (featureSupport != null) {
      result += sizeOf(34u) + sizeOf(featureSupport)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (deprecated != null) {
      writer.writeTag(8u).write(deprecated)
    }
    if (features != null) {
      writer.writeTag(18u).write(features)
    }
    if (debugRedact != null) {
      writer.writeTag(24u).write(debugRedact)
    }
    if (featureSupport != null) {
      writer.writeTag(34u).write(featureSupport)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is EnumValueOptions &&
      other.deprecated == this.deprecated &&
      other.features == this.features &&
      other.debugRedact == this.debugRedact &&
      other.featureSupport == this.featureSupport &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + debugRedact.hashCode()
    result = 31 * result + featureSupport.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "EnumValueOptions(" +
      "deprecated=$deprecated, " +
      "features=$features, " +
      "debugRedact=$debugRedact, " +
      "featureSupport=$featureSupport, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): EnumValueOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var deprecated: Boolean? = null

    public var features: FeatureSet? = null

    public var debugRedact: Boolean? = null

    public var featureSupport: FieldOptions.FeatureSupport? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): EnumValueOptions =
      EnumValueOptions(
        deprecated,
        features,
        debugRedact,
        featureSupport,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: EnumValueOptions): Builder =
        Builder().also {
          it.deprecated = msg.deprecated
          it.features = msg.features
          it.debugRedact = msg.debugRedact
          it.featureSupport = msg.featureSupport
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<EnumValueOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): EnumValueOptions {
      var deprecated: Boolean? = null
      var features: FeatureSet? = null
      var debugRedact: Boolean? = null
      var featureSupport: FieldOptions.FeatureSupport? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return EnumValueOptions(
              deprecated,
              features,
              debugRedact,
              featureSupport,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            deprecated = reader.readBool()
          }

          18u -> {
            features = reader.readMessage(FeatureSet)
          }

          24u -> {
            debugRedact = reader.readBool()
          }

          34u -> {
            featureSupport = reader.readMessage(FieldOptions.FeatureSupport)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

@GeneratedMessage("google.protobuf.ServiceOptions")
public class ServiceOptions private constructor(
  /**
   * Is this service deprecated? Depending on the target platform, this can emit Deprecated annotations for the service, or it will be completely ignored; in the very least, this is a formalization for deprecating services.
   */
  @GeneratedProperty(33)
  public val deprecated: Boolean?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(34)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (deprecated != null) {
      result += sizeOf(264u) + 1
    }
    if (features != null) {
      result += sizeOf(274u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (deprecated != null) {
      writer.writeTag(264u).write(deprecated)
    }
    if (features != null) {
      writer.writeTag(274u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is ServiceOptions &&
      other.deprecated == this.deprecated &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "ServiceOptions(" +
      "deprecated=$deprecated, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): ServiceOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var deprecated: Boolean? = null

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): ServiceOptions =
      ServiceOptions(
        deprecated,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: ServiceOptions): Builder =
        Builder().also {
          it.deprecated = msg.deprecated
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<ServiceOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): ServiceOptions {
      var deprecated: Boolean? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return ServiceOptions(
              deprecated,
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          264u -> {
            deprecated = reader.readBool()
          }

          274u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

@GeneratedMessage("google.protobuf.MethodOptions")
public class MethodOptions private constructor(
  /**
   * Is this method deprecated? Depending on the target platform, this can emit Deprecated annotations for the method, or it will be completely ignored; in the very least, this is a formalization for deprecating methods.
   */
  @GeneratedProperty(33)
  public val deprecated: Boolean?,
  @GeneratedProperty(34)
  public val idempotencyLevel: IdempotencyLevel?,
  /**
   * Any features defined in the specific edition. WARNING: This field should only be used by protobuf plugins or special cases like the proto compiler. Other uses are discouraged and developers should rely on the protoreflect APIs for their client language.
   */
  @GeneratedProperty(35)
  public val features: FeatureSet?,
  /**
   * The parser stores options it doesn't recognize here. See above.
   */
  @GeneratedProperty(999)
  public val uninterpretedOption: List<UninterpretedOption>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (deprecated != null) {
      result += sizeOf(264u) + 1
    }
    if (idempotencyLevel != null) {
      result += sizeOf(272u) + sizeOf(idempotencyLevel)
    }
    if (features != null) {
      result += sizeOf(282u) + sizeOf(features)
    }
    if (uninterpretedOption.isNotEmpty()) {
      result += (sizeOf(7994u) * uninterpretedOption.size) + uninterpretedOption.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (deprecated != null) {
      writer.writeTag(264u).write(deprecated)
    }
    if (idempotencyLevel != null) {
      writer.writeTag(272u).write(idempotencyLevel)
    }
    if (features != null) {
      writer.writeTag(282u).write(features)
    }
    uninterpretedOption.forEach { writer.writeTag(7994u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is MethodOptions &&
      other.deprecated == this.deprecated &&
      other.idempotencyLevel == this.idempotencyLevel &&
      other.features == this.features &&
      other.uninterpretedOption == this.uninterpretedOption &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + deprecated.hashCode()
    result = 31 * result + idempotencyLevel.hashCode()
    result = 31 * result + features.hashCode()
    result = 31 * result + uninterpretedOption.hashCode()
    return result
  }

  override fun toString(): String =
    "MethodOptions(" +
      "deprecated=$deprecated, " +
      "idempotencyLevel=$idempotencyLevel, " +
      "features=$features, " +
      "uninterpretedOption=$uninterpretedOption" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): MethodOptions =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var deprecated: Boolean? = null

    public var idempotencyLevel: IdempotencyLevel? = null

    public var features: FeatureSet? = null

    public var uninterpretedOption: List<UninterpretedOption> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): MethodOptions =
      MethodOptions(
        deprecated,
        idempotencyLevel,
        features,
        freezeList(uninterpretedOption),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: MethodOptions): Builder =
        Builder().also {
          it.deprecated = msg.deprecated
          it.idempotencyLevel = msg.idempotencyLevel
          it.features = msg.features
          it.uninterpretedOption = msg.uninterpretedOption
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<MethodOptions>() {
    @JvmStatic
    override fun deserialize(reader: Reader): MethodOptions {
      var deprecated: Boolean? = null
      var idempotencyLevel: IdempotencyLevel? = null
      var features: FeatureSet? = null
      var uninterpretedOption: ListBuilder<UninterpretedOption>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return MethodOptions(
              deprecated,
              idempotencyLevel,
              features,
              uninterpretedOption?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          264u -> {
            deprecated = reader.readBool()
          }

          272u -> {
            idempotencyLevel = reader.readEnum(IdempotencyLevel)
          }

          282u -> {
            features = reader.readMessage(FeatureSet)
          }

          7994u -> {
            uninterpretedOption =
              (uninterpretedOption ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(UninterpretedOption))
                }
              }
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

  /**
   * Is this method side-effect-free (or safe in HTTP parlance), or idempotent, or neither? HTTP based RPC implementation may choose GET verb for safe methods, and PUT verb for idempotent methods instead of the default POST.
   */
  public sealed class IdempotencyLevel(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object IDEMPOTENCY_UNKNOWN : IdempotencyLevel(0, "IDEMPOTENCY_UNKNOWN")

    public object NO_SIDE_EFFECTS : IdempotencyLevel(1, "NO_SIDE_EFFECTS")

    public object IDEMPOTENT : IdempotencyLevel(2, "IDEMPOTENT")

    public class UNRECOGNIZED(
      `value`: Int
    ) : IdempotencyLevel(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<IdempotencyLevel> {
      override fun deserialize(`value`: Int): IdempotencyLevel =
        when (value) {
          0 -> IDEMPOTENCY_UNKNOWN
          1 -> NO_SIDE_EFFECTS
          2 -> IDEMPOTENT
          else -> UNRECOGNIZED(value)
        }
    }
  }
}

/**
 * A message representing a option the parser does not recognize. This only appears in options protos created by the compiler::Parser class. DescriptorPool resolves these when building Descriptor objects. Therefore, options protos in descriptor objects (e.g. returned by Descriptor::options(), or produced by Descriptor::CopyTo()) will never have UninterpretedOptions in them.
 */
@GeneratedMessage("google.protobuf.UninterpretedOption")
public class UninterpretedOption private constructor(
  @GeneratedProperty(2)
  public val name: List<NamePart>,
  private val _identifierValue: LazyReference<Bytes, String>?,
  @GeneratedProperty(4)
  public val positiveIntValue: ULong?,
  @GeneratedProperty(5)
  public val negativeIntValue: Long?,
  @GeneratedProperty(6)
  public val doubleValue: Double?,
  @GeneratedProperty(7)
  public val stringValue: Bytes?,
  private val _aggregateValue: LazyReference<Bytes, String>?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (name.isNotEmpty()) {
      result += (sizeOf(18u) * name.size) + name.sumOf { sizeOf(it) }
    }
    if (_identifierValue != null) {
      result += sizeOf(26u) + sizeOf(_identifierValue.wireValue())
    }
    if (positiveIntValue != null) {
      result += sizeOf(32u) + sizeOf(positiveIntValue)
    }
    if (negativeIntValue != null) {
      result += sizeOf(40u) + sizeOf(negativeIntValue)
    }
    if (doubleValue != null) {
      result += sizeOf(49u) + 8
    }
    if (stringValue != null) {
      result += sizeOf(58u) + sizeOf(stringValue)
    }
    if (_aggregateValue != null) {
      result += sizeOf(66u) + sizeOf(_aggregateValue.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * The value of the uninterpreted option, in whatever type the tokenizer identified it as during parsing. Exactly one of these should be set.
   */
  @GeneratedProperty(3)
  public val identifierValue: String?
    get() = _identifierValue?.value()

  @GeneratedProperty(8)
  public val aggregateValue: String?
    get() = _aggregateValue?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    name.forEach { writer.writeTag(18u).write(it) }
    if (_identifierValue != null) {
      writer.writeTag(26u).write(_identifierValue.wireValue())
    }
    if (positiveIntValue != null) {
      writer.writeTag(32u).writeUInt64(positiveIntValue)
    }
    if (negativeIntValue != null) {
      writer.writeTag(40u).write(negativeIntValue)
    }
    if (doubleValue != null) {
      writer.writeTag(49u).write(doubleValue)
    }
    if (stringValue != null) {
      writer.writeTag(58u).write(stringValue)
    }
    if (_aggregateValue != null) {
      writer.writeTag(66u).write(_aggregateValue.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is UninterpretedOption &&
      other.name == this.name &&
      other.identifierValue == this.identifierValue &&
      other.positiveIntValue == this.positiveIntValue &&
      other.negativeIntValue == this.negativeIntValue &&
      other.doubleValue == this.doubleValue &&
      other.stringValue == this.stringValue &&
      other.aggregateValue == this.aggregateValue &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + identifierValue.hashCode()
    result = 31 * result + positiveIntValue.hashCode()
    result = 31 * result + negativeIntValue.hashCode()
    result = 31 * result + doubleValue.hashCode()
    result = 31 * result + stringValue.hashCode()
    result = 31 * result + aggregateValue.hashCode()
    return result
  }

  override fun toString(): String =
    "UninterpretedOption(" +
      "name=$name, " +
      "identifierValue=$identifierValue, " +
      "positiveIntValue=$positiveIntValue, " +
      "negativeIntValue=$negativeIntValue, " +
      "doubleValue=$doubleValue, " +
      "stringValue=$stringValue, " +
      "aggregateValue=$aggregateValue" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): UninterpretedOption =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var name: List<NamePart> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    private var _identifierValueRef: LazyReference<Bytes, String>? = null

    public var identifierValue: String?
      get() = _identifierValueRef?.value()
      set(newValue) {
        _identifierValueRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var positiveIntValue: ULong? = null

    public var negativeIntValue: Long? = null

    public var doubleValue: Double? = null

    public var stringValue: Bytes? = null

    private var _aggregateValueRef: LazyReference<Bytes, String>? = null

    public var aggregateValue: String?
      get() = _aggregateValueRef?.value()
      set(newValue) {
        _aggregateValueRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): UninterpretedOption =
      UninterpretedOption(
        freezeList(name),
        _identifierValueRef,
        positiveIntValue,
        negativeIntValue,
        doubleValue,
        stringValue,
        _aggregateValueRef,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: UninterpretedOption): Builder =
        Builder().also {
          it.name = msg.name
          it._identifierValueRef = msg._identifierValue
          it.positiveIntValue = msg.positiveIntValue
          it.negativeIntValue = msg.negativeIntValue
          it.doubleValue = msg.doubleValue
          it.stringValue = msg.stringValue
          it._aggregateValueRef = msg._aggregateValue
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<UninterpretedOption>() {
    @JvmStatic
    override fun deserialize(reader: Reader): UninterpretedOption {
      var name: ListBuilder<NamePart>? = null
      var identifierValue: Bytes? = null
      var positiveIntValue: ULong? = null
      var negativeIntValue: Long? = null
      var doubleValue: Double? = null
      var stringValue: Bytes? = null
      var aggregateValue: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return UninterpretedOption(
              name?.build() ?: emptyList(),
              identifierValue?.let { LazyReference(it, StringConverter) },
              positiveIntValue,
              negativeIntValue,
              doubleValue,
              stringValue,
              aggregateValue?.let { LazyReference(it, StringConverter) },
              UnknownFieldSet.from(unknownFields)
            )
          }

          18u -> {
            name =
              (name ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(NamePart))
                }
              }
          }

          26u -> {
            identifierValue = StringConverter.readValidatedBytes(reader)
          }

          32u -> {
            positiveIntValue = reader.readUInt64()
          }

          40u -> {
            negativeIntValue = reader.readInt64()
          }

          49u -> {
            doubleValue = reader.readDouble()
          }

          58u -> {
            stringValue = reader.readBytes()
          }

          66u -> {
            aggregateValue = StringConverter.readValidatedBytes(reader)
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
    public operator fun invoke(dsl: Builder.() -> Unit): UninterpretedOption =
      Builder().apply(dsl).build()
  }

  /**
   * The name of the uninterpreted option.  Each string represents a segment in a dot-separated name.  is_extension is true iff a segment represents an extension (denoted with parentheses in options specs in .proto files). E.g.,{ ["foo", false], ["bar.baz", true], ["moo", false] } represents "foo.(bar.baz).moo".
   */
  @GeneratedMessage("google.protobuf.UninterpretedOption.NamePart")
  public class NamePart private constructor(
    private val _namePart: LazyReference<Bytes, String>,
    @GeneratedProperty(2)
    public val isExtension: Boolean,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (_namePart.wireValue().isNotEmpty()) {
        result += sizeOf(10u) + sizeOf(_namePart.wireValue())
      }
      if (isExtension) {
        result += sizeOf(16u) + 1
      }
      result += unknownFields.size()
      result
    }

    @GeneratedProperty(1)
    public val namePart: String
      get() = _namePart.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (_namePart.wireValue().isNotEmpty()) {
        writer.writeTag(10u).write(_namePart.wireValue())
      }
      if (isExtension) {
        writer.writeTag(16u).write(isExtension)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is NamePart &&
        other.namePart == this.namePart &&
        other.isExtension == this.isExtension &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + namePart.hashCode()
      result = 31 * result + isExtension.hashCode()
      return result
    }

    override fun toString(): String =
      "NamePart(" +
        "namePart=$namePart, " +
        "isExtension=$isExtension" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): NamePart =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      private var _namePartRef: LazyReference<Bytes, String>? = null

      public var namePart: String
        get() = _namePartRef?.value() ?: ""
        set(newValue) {
          _namePartRef = LazyReference(newValue, StringConverter)
        }

      public var isExtension: Boolean = false

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): NamePart =
        NamePart(
          _namePartRef ?: LazyReference(Bytes.empty(), StringConverter),
          isExtension,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: NamePart): Builder =
          Builder().also {
            it._namePartRef = msg._namePart
            it.isExtension = msg.isExtension
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<NamePart>() {
      @JvmStatic
      override fun deserialize(reader: Reader): NamePart {
        var namePart: Bytes? = null
        var isExtension = false
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return NamePart(
                LazyReference(namePart ?: Bytes.empty(), StringConverter),
                isExtension,
                UnknownFieldSet.from(unknownFields)
              )
            }

            10u -> {
              namePart = StringConverter.readValidatedBytes(reader)
            }

            16u -> {
              isExtension = reader.readBool()
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
      public operator fun invoke(dsl: Builder.() -> Unit): NamePart =
        Builder().apply(dsl).build()
    }
  }
}

/**
 * TODO Enums in C++ gencode (and potentially other languages) are not well scoped.  This means that each of the feature enums below can clash with each other.  The short names we've chosen maximize call-site readability, but leave us very open to this scenario.  A future feature will be designed and implemented to handle this, hopefully before we ever hit a conflict here.
 */
@GeneratedMessage("google.protobuf.FeatureSet")
public class FeatureSet private constructor(
  @GeneratedProperty(1)
  public val fieldPresence: FieldPresence?,
  @GeneratedProperty(2)
  public val enumType: EnumType?,
  @GeneratedProperty(3)
  public val repeatedFieldEncoding: RepeatedFieldEncoding?,
  @GeneratedProperty(4)
  public val utf8Validation: Utf8Validation?,
  @GeneratedProperty(5)
  public val messageEncoding: MessageEncoding?,
  @GeneratedProperty(6)
  public val jsonFormat: JsonFormat?,
  @GeneratedProperty(7)
  public val enforceNamingStyle: EnforceNamingStyle?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (fieldPresence != null) {
      result += sizeOf(8u) + sizeOf(fieldPresence)
    }
    if (enumType != null) {
      result += sizeOf(16u) + sizeOf(enumType)
    }
    if (repeatedFieldEncoding != null) {
      result += sizeOf(24u) + sizeOf(repeatedFieldEncoding)
    }
    if (utf8Validation != null) {
      result += sizeOf(32u) + sizeOf(utf8Validation)
    }
    if (messageEncoding != null) {
      result += sizeOf(40u) + sizeOf(messageEncoding)
    }
    if (jsonFormat != null) {
      result += sizeOf(48u) + sizeOf(jsonFormat)
    }
    if (enforceNamingStyle != null) {
      result += sizeOf(56u) + sizeOf(enforceNamingStyle)
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (fieldPresence != null) {
      writer.writeTag(8u).write(fieldPresence)
    }
    if (enumType != null) {
      writer.writeTag(16u).write(enumType)
    }
    if (repeatedFieldEncoding != null) {
      writer.writeTag(24u).write(repeatedFieldEncoding)
    }
    if (utf8Validation != null) {
      writer.writeTag(32u).write(utf8Validation)
    }
    if (messageEncoding != null) {
      writer.writeTag(40u).write(messageEncoding)
    }
    if (jsonFormat != null) {
      writer.writeTag(48u).write(jsonFormat)
    }
    if (enforceNamingStyle != null) {
      writer.writeTag(56u).write(enforceNamingStyle)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FeatureSet &&
      other.fieldPresence == this.fieldPresence &&
      other.enumType == this.enumType &&
      other.repeatedFieldEncoding == this.repeatedFieldEncoding &&
      other.utf8Validation == this.utf8Validation &&
      other.messageEncoding == this.messageEncoding &&
      other.jsonFormat == this.jsonFormat &&
      other.enforceNamingStyle == this.enforceNamingStyle &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + fieldPresence.hashCode()
    result = 31 * result + enumType.hashCode()
    result = 31 * result + repeatedFieldEncoding.hashCode()
    result = 31 * result + utf8Validation.hashCode()
    result = 31 * result + messageEncoding.hashCode()
    result = 31 * result + jsonFormat.hashCode()
    result = 31 * result + enforceNamingStyle.hashCode()
    return result
  }

  override fun toString(): String =
    "FeatureSet(" +
      "fieldPresence=$fieldPresence, " +
      "enumType=$enumType, " +
      "repeatedFieldEncoding=$repeatedFieldEncoding, " +
      "utf8Validation=$utf8Validation, " +
      "messageEncoding=$messageEncoding, " +
      "jsonFormat=$jsonFormat, " +
      "enforceNamingStyle=$enforceNamingStyle" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FeatureSet =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var fieldPresence: FieldPresence? = null

    public var enumType: EnumType? = null

    public var repeatedFieldEncoding: RepeatedFieldEncoding? = null

    public var utf8Validation: Utf8Validation? = null

    public var messageEncoding: MessageEncoding? = null

    public var jsonFormat: JsonFormat? = null

    public var enforceNamingStyle: EnforceNamingStyle? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FeatureSet =
      FeatureSet(
        fieldPresence,
        enumType,
        repeatedFieldEncoding,
        utf8Validation,
        messageEncoding,
        jsonFormat,
        enforceNamingStyle,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FeatureSet): Builder =
        Builder().also {
          it.fieldPresence = msg.fieldPresence
          it.enumType = msg.enumType
          it.repeatedFieldEncoding = msg.repeatedFieldEncoding
          it.utf8Validation = msg.utf8Validation
          it.messageEncoding = msg.messageEncoding
          it.jsonFormat = msg.jsonFormat
          it.enforceNamingStyle = msg.enforceNamingStyle
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FeatureSet>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FeatureSet {
      var fieldPresence: FieldPresence? = null
      var enumType: EnumType? = null
      var repeatedFieldEncoding: RepeatedFieldEncoding? = null
      var utf8Validation: Utf8Validation? = null
      var messageEncoding: MessageEncoding? = null
      var jsonFormat: JsonFormat? = null
      var enforceNamingStyle: EnforceNamingStyle? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FeatureSet(
              fieldPresence,
              enumType,
              repeatedFieldEncoding,
              utf8Validation,
              messageEncoding,
              jsonFormat,
              enforceNamingStyle,
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            fieldPresence = reader.readEnum(FieldPresence)
          }

          16u -> {
            enumType = reader.readEnum(EnumType)
          }

          24u -> {
            repeatedFieldEncoding = reader.readEnum(RepeatedFieldEncoding)
          }

          32u -> {
            utf8Validation = reader.readEnum(Utf8Validation)
          }

          40u -> {
            messageEncoding = reader.readEnum(MessageEncoding)
          }

          48u -> {
            jsonFormat = reader.readEnum(JsonFormat)
          }

          56u -> {
            enforceNamingStyle = reader.readEnum(EnforceNamingStyle)
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
    public operator fun invoke(dsl: Builder.() -> Unit): FeatureSet =
      Builder().apply(dsl).build()
  }

  public sealed class FieldPresence(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object FIELD_PRESENCE_UNKNOWN : FieldPresence(0, "FIELD_PRESENCE_UNKNOWN")

    public object EXPLICIT : FieldPresence(1, "EXPLICIT")

    public object IMPLICIT : FieldPresence(2, "IMPLICIT")

    public object LEGACY_REQUIRED : FieldPresence(3, "LEGACY_REQUIRED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : FieldPresence(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<FieldPresence> {
      override fun deserialize(`value`: Int): FieldPresence =
        when (value) {
          0 -> FIELD_PRESENCE_UNKNOWN
          1 -> EXPLICIT
          2 -> IMPLICIT
          3 -> LEGACY_REQUIRED
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class EnumType(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object ENUM_TYPE_UNKNOWN : EnumType(0, "ENUM_TYPE_UNKNOWN")

    public object OPEN : EnumType(1, "OPEN")

    public object CLOSED : EnumType(2, "CLOSED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : EnumType(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<EnumType> {
      override fun deserialize(`value`: Int): EnumType =
        when (value) {
          0 -> ENUM_TYPE_UNKNOWN
          1 -> OPEN
          2 -> CLOSED
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class RepeatedFieldEncoding(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object REPEATED_FIELD_ENCODING_UNKNOWN : RepeatedFieldEncoding(0, "REPEATED_FIELD_ENCODING_UNKNOWN")

    public object PACKED : RepeatedFieldEncoding(1, "PACKED")

    public object EXPANDED : RepeatedFieldEncoding(2, "EXPANDED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : RepeatedFieldEncoding(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<RepeatedFieldEncoding> {
      override fun deserialize(`value`: Int): RepeatedFieldEncoding =
        when (value) {
          0 -> REPEATED_FIELD_ENCODING_UNKNOWN
          1 -> PACKED
          2 -> EXPANDED
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class Utf8Validation(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object UTF8_VALIDATION_UNKNOWN : Utf8Validation(0, "UTF8_VALIDATION_UNKNOWN")

    public object VERIFY : Utf8Validation(2, "VERIFY")

    public object NONE : Utf8Validation(3, "NONE")

    public class UNRECOGNIZED(
      `value`: Int
    ) : Utf8Validation(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<Utf8Validation> {
      override fun deserialize(`value`: Int): Utf8Validation =
        when (value) {
          0 -> UTF8_VALIDATION_UNKNOWN
          2 -> VERIFY
          3 -> NONE
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class MessageEncoding(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object MESSAGE_ENCODING_UNKNOWN : MessageEncoding(0, "MESSAGE_ENCODING_UNKNOWN")

    public object LENGTH_PREFIXED : MessageEncoding(1, "LENGTH_PREFIXED")

    public object DELIMITED : MessageEncoding(2, "DELIMITED")

    public class UNRECOGNIZED(
      `value`: Int
    ) : MessageEncoding(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<MessageEncoding> {
      override fun deserialize(`value`: Int): MessageEncoding =
        when (value) {
          0 -> MESSAGE_ENCODING_UNKNOWN
          1 -> LENGTH_PREFIXED
          2 -> DELIMITED
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class JsonFormat(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object JSON_FORMAT_UNKNOWN : JsonFormat(0, "JSON_FORMAT_UNKNOWN")

    public object ALLOW : JsonFormat(1, "ALLOW")

    public object LEGACY_BEST_EFFORT : JsonFormat(2, "LEGACY_BEST_EFFORT")

    public class UNRECOGNIZED(
      `value`: Int
    ) : JsonFormat(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<JsonFormat> {
      override fun deserialize(`value`: Int): JsonFormat =
        when (value) {
          0 -> JSON_FORMAT_UNKNOWN
          1 -> ALLOW
          2 -> LEGACY_BEST_EFFORT
          else -> UNRECOGNIZED(value)
        }
    }
  }

  public sealed class EnforceNamingStyle(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object ENFORCE_NAMING_STYLE_UNKNOWN : EnforceNamingStyle(0, "ENFORCE_NAMING_STYLE_UNKNOWN")

    public object STYLE2024 : EnforceNamingStyle(1, "STYLE2024")

    public object STYLE_LEGACY : EnforceNamingStyle(2, "STYLE_LEGACY")

    public class UNRECOGNIZED(
      `value`: Int
    ) : EnforceNamingStyle(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<EnforceNamingStyle> {
      override fun deserialize(`value`: Int): EnforceNamingStyle =
        when (value) {
          0 -> ENFORCE_NAMING_STYLE_UNKNOWN
          1 -> STYLE2024
          2 -> STYLE_LEGACY
          else -> UNRECOGNIZED(value)
        }
    }
  }
}

/**
 * A compiled specification for the defaults of a set of features.  These messages are generated from FeatureSet extensions and can be used to seed feature resolution. The resolution with this object becomes a simple search for the closest matching edition, followed by proto merges.
 */
@GeneratedMessage("google.protobuf.FeatureSetDefaults")
public class FeatureSetDefaults private constructor(
  @GeneratedProperty(1)
  public val defaults: List<FeatureSetEditionDefault>,
  /**
   * The minimum supported edition (inclusive) when this was constructed. Editions before this will not have defaults.
   */
  @GeneratedProperty(4)
  public val minimumEdition: Edition?,
  /**
   * The maximum known edition (inclusive) when this was constructed. Editions after this will not have reliable defaults.
   */
  @GeneratedProperty(5)
  public val maximumEdition: Edition?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (defaults.isNotEmpty()) {
      result += (sizeOf(10u) * defaults.size) + defaults.sumOf { sizeOf(it) }
    }
    if (minimumEdition != null) {
      result += sizeOf(32u) + sizeOf(minimumEdition)
    }
    if (maximumEdition != null) {
      result += sizeOf(40u) + sizeOf(maximumEdition)
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    defaults.forEach { writer.writeTag(10u).write(it) }
    if (minimumEdition != null) {
      writer.writeTag(32u).write(minimumEdition)
    }
    if (maximumEdition != null) {
      writer.writeTag(40u).write(maximumEdition)
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is FeatureSetDefaults &&
      other.defaults == this.defaults &&
      other.minimumEdition == this.minimumEdition &&
      other.maximumEdition == this.maximumEdition &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + defaults.hashCode()
    result = 31 * result + minimumEdition.hashCode()
    result = 31 * result + maximumEdition.hashCode()
    return result
  }

  override fun toString(): String =
    "FeatureSetDefaults(" +
      "defaults=$defaults, " +
      "minimumEdition=$minimumEdition, " +
      "maximumEdition=$maximumEdition" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): FeatureSetDefaults =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var defaults: List<FeatureSetEditionDefault> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var minimumEdition: Edition? = null

    public var maximumEdition: Edition? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): FeatureSetDefaults =
      FeatureSetDefaults(
        freezeList(defaults),
        minimumEdition,
        maximumEdition,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: FeatureSetDefaults): Builder =
        Builder().also {
          it.defaults = msg.defaults
          it.minimumEdition = msg.minimumEdition
          it.maximumEdition = msg.maximumEdition
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<FeatureSetDefaults>() {
    @JvmStatic
    override fun deserialize(reader: Reader): FeatureSetDefaults {
      var defaults: ListBuilder<FeatureSetEditionDefault>? = null
      var minimumEdition: Edition? = null
      var maximumEdition: Edition? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return FeatureSetDefaults(
              defaults?.build() ?: emptyList(),
              minimumEdition,
              maximumEdition,
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            defaults =
              (defaults ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FeatureSetEditionDefault))
                }
              }
          }

          32u -> {
            minimumEdition = reader.readEnum(Edition)
          }

          40u -> {
            maximumEdition = reader.readEnum(Edition)
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
    public operator fun invoke(dsl: Builder.() -> Unit): FeatureSetDefaults =
      Builder().apply(dsl).build()
  }

  /**
   * A map from every known edition with a unique set of defaults to its defaults. Not all editions may be contained here.  For a given edition, the defaults at the closest matching edition ordered at or before it should be used.  This field must be in strict ascending order by edition.
   */
  @GeneratedMessage("google.protobuf.FeatureSetDefaults.FeatureSetEditionDefault")
  public class FeatureSetEditionDefault private constructor(
    @GeneratedProperty(3)
    public val edition: Edition?,
    /**
     * Defaults of features that can be overridden in this edition.
     */
    @GeneratedProperty(4)
    public val overridableFeatures: FeatureSet?,
    /**
     * Defaults of features that can't be overridden in this edition.
     */
    @GeneratedProperty(5)
    public val fixedFeatures: FeatureSet?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (edition != null) {
        result += sizeOf(24u) + sizeOf(edition)
      }
      if (overridableFeatures != null) {
        result += sizeOf(34u) + sizeOf(overridableFeatures)
      }
      if (fixedFeatures != null) {
        result += sizeOf(42u) + sizeOf(fixedFeatures)
      }
      result += unknownFields.size()
      result
    }

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (edition != null) {
        writer.writeTag(24u).write(edition)
      }
      if (overridableFeatures != null) {
        writer.writeTag(34u).write(overridableFeatures)
      }
      if (fixedFeatures != null) {
        writer.writeTag(42u).write(fixedFeatures)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is FeatureSetEditionDefault &&
        other.edition == this.edition &&
        other.overridableFeatures == this.overridableFeatures &&
        other.fixedFeatures == this.fixedFeatures &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + edition.hashCode()
      result = 31 * result + overridableFeatures.hashCode()
      result = 31 * result + fixedFeatures.hashCode()
      return result
    }

    override fun toString(): String =
      "FeatureSetEditionDefault(" +
        "edition=$edition, " +
        "overridableFeatures=$overridableFeatures, " +
        "fixedFeatures=$fixedFeatures" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): FeatureSetEditionDefault =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var edition: Edition? = null

      public var overridableFeatures: FeatureSet? = null

      public var fixedFeatures: FeatureSet? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): FeatureSetEditionDefault =
        FeatureSetEditionDefault(
          edition,
          overridableFeatures,
          fixedFeatures,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: FeatureSetEditionDefault): Builder =
          Builder().also {
            it.edition = msg.edition
            it.overridableFeatures = msg.overridableFeatures
            it.fixedFeatures = msg.fixedFeatures
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<FeatureSetEditionDefault>() {
      @JvmStatic
      override fun deserialize(reader: Reader): FeatureSetEditionDefault {
        var edition: Edition? = null
        var overridableFeatures: FeatureSet? = null
        var fixedFeatures: FeatureSet? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return FeatureSetEditionDefault(
                edition,
                overridableFeatures,
                fixedFeatures,
                UnknownFieldSet.from(unknownFields)
              )
            }

            24u -> {
              edition = reader.readEnum(Edition)
            }

            34u -> {
              overridableFeatures = reader.readMessage(FeatureSet)
            }

            42u -> {
              fixedFeatures = reader.readMessage(FeatureSet)
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
      public operator fun invoke(dsl: Builder.() -> Unit): FeatureSetEditionDefault =
        Builder().apply(dsl).build()
    }
  }
}

/**
 * Encapsulates information about the original source file from which a FileDescriptorProto was generated.
 */
@GeneratedMessage("google.protobuf.SourceCodeInfo")
public class SourceCodeInfo private constructor(
  /**
   * A Location identifies a piece of source code in a .proto file which corresponds to a particular definition.  This information is intended to be useful to IDEs, code indexers, documentation generators, and similar tools.
   *
   *  For example, say we have a file like:   message Foo {     optional string foo = 1;   } Let's look at just the field definition:   optional string foo = 1;   ^       ^^     ^^  ^  ^^^   a       bc     de  f  ghi We have the following locations:   span   path               represents   [a,i)  [ 4, 0, 2, 0 ]     The whole field definition.   [a,b)  [ 4, 0, 2, 0, 4 ]  The label (optional).   [c,d)  [ 4, 0, 2, 0, 5 ]  The type (string).   [e,f)  [ 4, 0, 2, 0, 1 ]  The name (foo).   [g,h)  [ 4, 0, 2, 0, 3 ]  The number (1).
   *
   *  Notes: - A location may refer to a repeated field itself (i.e. not to any   particular index within it).  This is used whenever a set of elements are   logically enclosed in a single code segment.  For example, an entire   extend block (possibly containing multiple extension definitions) will   have an outer location whose path refers to the "extensions" repeated   field without an index. - Multiple locations may have the same path.  This happens when a single   logical declaration is spread out across multiple places.  The most   obvious example is the "extend" block again -- there may be multiple   extend blocks in the same scope, each of which will have the same path. - A location's span is not always a subset of its parent's span.  For   example, the "extendee" of an extension declaration appears at the   beginning of the "extend" block and is shared by all extensions within   the block. - Just because a location's span is a subset of some other location's span   does not mean that it is a descendant.  For example, a "group" defines   both a type and a field in a single declaration.  Thus, the locations   corresponding to the type and field and their components will overlap. - Code which tries to interpret locations should probably be designed to   ignore those that it doesn't understand, as more types of locations could   be recorded in the future.
   */
  @GeneratedProperty(1)
  public val location: List<Location>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (location.isNotEmpty()) {
      result += (sizeOf(10u) * location.size) + location.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    location.forEach { writer.writeTag(10u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is SourceCodeInfo &&
      other.location == this.location &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + location.hashCode()
    return result
  }

  override fun toString(): String =
    "SourceCodeInfo(" +
      "location=$location" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): SourceCodeInfo =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var location: List<Location> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): SourceCodeInfo =
      SourceCodeInfo(
        freezeList(location),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: SourceCodeInfo): Builder =
        Builder().also {
          it.location = msg.location
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<SourceCodeInfo>() {
    @JvmStatic
    override fun deserialize(reader: Reader): SourceCodeInfo {
      var location: ListBuilder<Location>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return SourceCodeInfo(
              location?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            location =
              (location ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(Location))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): SourceCodeInfo =
      Builder().apply(dsl).build()
  }

  @GeneratedMessage("google.protobuf.SourceCodeInfo.Location")
  public class Location private constructor(
    /**
     * Identifies which part of the FileDescriptorProto was defined at this location.
     *
     *  Each element is a field number or an index.  They form a path from the root FileDescriptorProto to the place where the definition appears. For example, this path:   [ 4, 3, 2, 7, 1 ] refers to:   file.message_type(3)  // 4, 3       .field(7)         // 2, 7       .name()           // 1 This is because FileDescriptorProto.message_type has field number 4:   repeated DescriptorProto message_type = 4; and DescriptorProto.field has field number 2:   repeated FieldDescriptorProto field = 2; and FieldDescriptorProto.name has field number 1:   optional string name = 1;
     *
     *  Thus, the above path gives the location of a field name.  If we removed the last element:   [ 4, 3, 2, 7 ] this path refers to the whole field declaration (from the beginning of the label to the terminating semicolon).
     */
    @GeneratedProperty(1)
    public val path: List<Int>,
    /**
     * Always has exactly three or four elements: start line, start column, end line (optional, otherwise assumed same as start line), end column. These are packed into a single field for efficiency.  Note that line and column numbers are zero-based -- typically you will want to add 1 to each before displaying to a user.
     */
    @GeneratedProperty(2)
    public val span: List<Int>,
    private val _leadingComments: LazyReference<Bytes, String>?,
    private val _trailingComments: LazyReference<Bytes, String>?,
    @GeneratedProperty(6)
    public val leadingDetachedComments: List<String>,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (path.isNotEmpty()) {
        result += sizeOf(10u) + path.sumOf { sizeOf(it) }.let { it + sizeOf(it.toUInt()) }
      }
      if (span.isNotEmpty()) {
        result += sizeOf(18u) + span.sumOf { sizeOf(it) }.let { it + sizeOf(it.toUInt()) }
      }
      if (_leadingComments != null) {
        result += sizeOf(26u) + sizeOf(_leadingComments.wireValue())
      }
      if (_trailingComments != null) {
        result += sizeOf(34u) + sizeOf(_trailingComments.wireValue())
      }
      if (leadingDetachedComments.isNotEmpty()) {
        result +=
          @Suppress("UNCHECKED_CAST")
          (leadingDetachedComments as LazyConvertingList<Bytes, Any>).let { list ->
            (sizeOf(50u) * list.size) +
              run {
                var sum = 0
                for (i in list.indices) sum += sizeOf(list.wireGet(i))
                sum
              }
          }
      }
      result += unknownFields.size()
      result
    }

    /**
     * If this SourceCodeInfo represents a complete declaration, these are any comments appearing before and after the declaration which appear to be attached to the declaration.
     *
     *  A series of line comments appearing on consecutive lines, with no other tokens appearing on those lines, will be treated as a single comment.
     *
     *  leading_detached_comments will keep paragraphs of comments that appear before (but not connected to) the current element. Each paragraph, separated by empty lines, will be one comment element in the repeated field.
     *
     *  Only the comment content is provided; comment markers (e.g. //) are stripped out.  For block comments, leading whitespace and an asterisk will be stripped from the beginning of each line other than the first. Newlines are included in the output.
     *
     *  Examples:
     *
     *    optional int32 foo = 1;  // Comment attached to foo.   // Comment attached to bar.   optional int32 bar = 2;
     *
     *    optional string baz = 3;   // Comment attached to baz.   // Another line attached to baz.
     *
     *    // Comment attached to moo.   //   // Another line attached to moo.   optional double moo = 4;
     *
     *    // Detached comment for corge. This is not leading or trailing comments   // to moo or corge because there are blank lines separating it from   // both.
     *
     *    // Detached comment for corge paragraph 2.
     *
     *    optional string corge = 5;   &#47;* Block comment attached    * to corge.  Leading asterisks    * will be removed. *&#47;   &#47;* Block comment attached to    * grault. *&#47;   optional int32 grault = 6;
     *
     *    // ignored detached comments.
     */
    @GeneratedProperty(3)
    public val leadingComments: String?
      get() = _leadingComments?.value()

    @GeneratedProperty(4)
    public val trailingComments: String?
      get() = _trailingComments?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (path.isNotEmpty()) {
        writer.writeTag(10u).writeUInt32(path.sumOf { sizeOf(it) }.toUInt())
        path.forEach { writer.write(it) }
      }
      if (span.isNotEmpty()) {
        writer.writeTag(18u).writeUInt32(span.sumOf { sizeOf(it) }.toUInt())
        span.forEach { writer.write(it) }
      }
      if (_leadingComments != null) {
        writer.writeTag(26u).write(_leadingComments.wireValue())
      }
      if (_trailingComments != null) {
        writer.writeTag(34u).write(_trailingComments.wireValue())
      }
      if (leadingDetachedComments.isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        (leadingDetachedComments as LazyConvertingList<Bytes, Any>).wireForEach { writer.writeTag(50u).write(it) }
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is Location &&
        other.path == this.path &&
        other.span == this.span &&
        other.leadingComments == this.leadingComments &&
        other.trailingComments == this.trailingComments &&
        other.leadingDetachedComments == this.leadingDetachedComments &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + path.hashCode()
      result = 31 * result + span.hashCode()
      result = 31 * result + leadingComments.hashCode()
      result = 31 * result + trailingComments.hashCode()
      result = 31 * result + leadingDetachedComments.hashCode()
      return result
    }

    override fun toString(): String =
      "Location(" +
        "path=$path, " +
        "span=$span, " +
        "leadingComments=$leadingComments, " +
        "trailingComments=$trailingComments, " +
        "leadingDetachedComments=$leadingDetachedComments" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): Location =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var path: List<Int> = emptyList()
        set(newValue) {
          field = freezeList(newValue)
        }

      public var span: List<Int> = emptyList()
        set(newValue) {
          field = freezeList(newValue)
        }

      private var _leadingCommentsRef: LazyReference<Bytes, String>? = null

      public var leadingComments: String?
        get() = _leadingCommentsRef?.value()
        set(newValue) {
          _leadingCommentsRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      private var _trailingCommentsRef: LazyReference<Bytes, String>? = null

      public var trailingComments: String?
        get() = _trailingCommentsRef?.value()
        set(newValue) {
          _trailingCommentsRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var leadingDetachedComments: List<String> = emptyList()
        set(newValue) {
          field = if (newValue is LazyConvertingList<*, *>) newValue else LazyConvertingList.fromKotlin(newValue, StringConverter)
        }

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): Location =
        Location(
          freezeList(path),
          freezeList(span),
          _leadingCommentsRef,
          _trailingCommentsRef,
          leadingDetachedComments,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: Location): Builder =
          Builder().also {
            it.path = msg.path
            it.span = msg.span
            it._leadingCommentsRef = msg._leadingComments
            it._trailingCommentsRef = msg._trailingComments
            it.leadingDetachedComments = msg.leadingDetachedComments
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<Location>() {
      @JvmStatic
      override fun deserialize(reader: Reader): Location {
        var path: ListBuilder<Int>? = null
        var span: ListBuilder<Int>? = null
        var leadingComments: Bytes? = null
        var trailingComments: Bytes? = null
        var leadingDetachedComments: ListBuilder<Any?>? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return Location(
                path?.build() ?: emptyList(),
                span?.build() ?: emptyList(),
                leadingComments?.let { LazyReference(it, StringConverter) },
                trailingComments?.let { LazyReference(it, StringConverter) },
                leadingDetachedComments?.build()?.let { LazyConvertingList<Bytes, String>(it, StringConverter) } ?: emptyList(),
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              path =
                (path ?: listBuilder()).apply {
                  reader.readRepeated(false) {
                    add(reader.readInt32())
                  }
                }
            }

            10u -> {
              path =
                (path ?: listBuilder()).apply {
                  reader.readRepeated(true) {
                    add(reader.readInt32())
                  }
                }
            }

            16u -> {
              span =
                (span ?: listBuilder()).apply {
                  reader.readRepeated(false) {
                    add(reader.readInt32())
                  }
                }
            }

            18u -> {
              span =
                (span ?: listBuilder()).apply {
                  reader.readRepeated(true) {
                    add(reader.readInt32())
                  }
                }
            }

            26u -> {
              leadingComments = StringConverter.readValidatedBytes(reader)
            }

            34u -> {
              trailingComments = StringConverter.readValidatedBytes(reader)
            }

            50u -> {
              leadingDetachedComments =
                (leadingDetachedComments ?: listBuilder()).apply {
                  reader.readRepeated(false) {
                    add(LazyReference(StringConverter.readValidatedBytes(reader), StringConverter))
                  }
                }
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
      public operator fun invoke(dsl: Builder.() -> Unit): Location =
        Builder().apply(dsl).build()
    }
  }
}

/**
 * Describes the relationship between generated code and its original source file. A GeneratedCodeInfo message is associated with only one generated source file, but may contain references to different source .proto files.
 */
@GeneratedMessage("google.protobuf.GeneratedCodeInfo")
public class GeneratedCodeInfo private constructor(
  /**
   * An Annotation connects some span of text in generated code to an element of its generating .proto file.
   */
  @GeneratedProperty(1)
  public val `annotation`: List<Annotation>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (`annotation`.isNotEmpty()) {
      result += (sizeOf(10u) * `annotation`.size) + `annotation`.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    `annotation`.forEach { writer.writeTag(10u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is GeneratedCodeInfo &&
      other.`annotation` == this.`annotation` &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + `annotation`.hashCode()
    return result
  }

  override fun toString(): String =
    "GeneratedCodeInfo(" +
      "`annotation`=$`annotation`" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): GeneratedCodeInfo =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var `annotation`: List<Annotation> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): GeneratedCodeInfo =
      GeneratedCodeInfo(
        freezeList(`annotation`),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: GeneratedCodeInfo): Builder =
        Builder().also {
          it.`annotation` = msg.`annotation`
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<GeneratedCodeInfo>() {
    @JvmStatic
    override fun deserialize(reader: Reader): GeneratedCodeInfo {
      var `annotation`: ListBuilder<Annotation>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return GeneratedCodeInfo(
              `annotation`?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            `annotation` =
              (`annotation` ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(Annotation))
                }
              }
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
    public operator fun invoke(dsl: Builder.() -> Unit): GeneratedCodeInfo =
      Builder().apply(dsl).build()
  }

  @GeneratedMessage("google.protobuf.GeneratedCodeInfo.Annotation")
  public class Annotation private constructor(
    /**
     * Identifies the element in the original source .proto file. This field is formatted the same as SourceCodeInfo.Location.path.
     */
    @GeneratedProperty(1)
    public val path: List<Int>,
    private val _sourceFile: LazyReference<Bytes, String>?,
    /**
     * Identifies the starting offset in bytes in the generated code that relates to the identified object.
     */
    @GeneratedProperty(3)
    public val begin: Int?,
    /**
     * Identifies the ending offset in bytes in the generated code that relates to the identified object. The end offset should be one past the last relevant byte (so the length of the text = end - begin).
     */
    @GeneratedProperty(4)
    public val end: Int?,
    @GeneratedProperty(5)
    public val semantic: Semantic?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (path.isNotEmpty()) {
        result += sizeOf(10u) + path.sumOf { sizeOf(it) }.let { it + sizeOf(it.toUInt()) }
      }
      if (_sourceFile != null) {
        result += sizeOf(18u) + sizeOf(_sourceFile.wireValue())
      }
      if (begin != null) {
        result += sizeOf(24u) + sizeOf(begin)
      }
      if (end != null) {
        result += sizeOf(32u) + sizeOf(end)
      }
      if (semantic != null) {
        result += sizeOf(40u) + sizeOf(semantic)
      }
      result += unknownFields.size()
      result
    }

    /**
     * Identifies the filesystem path to the original source .proto.
     */
    @GeneratedProperty(2)
    public val sourceFile: String?
      get() = _sourceFile?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (path.isNotEmpty()) {
        writer.writeTag(10u).writeUInt32(path.sumOf { sizeOf(it) }.toUInt())
        path.forEach { writer.write(it) }
      }
      if (_sourceFile != null) {
        writer.writeTag(18u).write(_sourceFile.wireValue())
      }
      if (begin != null) {
        writer.writeTag(24u).write(begin)
      }
      if (end != null) {
        writer.writeTag(32u).write(end)
      }
      if (semantic != null) {
        writer.writeTag(40u).write(semantic)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is Annotation &&
        other.path == this.path &&
        other.sourceFile == this.sourceFile &&
        other.begin == this.begin &&
        other.end == this.end &&
        other.semantic == this.semantic &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + path.hashCode()
      result = 31 * result + sourceFile.hashCode()
      result = 31 * result + begin.hashCode()
      result = 31 * result + end.hashCode()
      result = 31 * result + semantic.hashCode()
      return result
    }

    override fun toString(): String =
      "Annotation(" +
        "path=$path, " +
        "sourceFile=$sourceFile, " +
        "begin=$begin, " +
        "end=$end, " +
        "semantic=$semantic" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): Annotation =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      public var path: List<Int> = emptyList()
        set(newValue) {
          field = freezeList(newValue)
        }

      private var _sourceFileRef: LazyReference<Bytes, String>? = null

      public var sourceFile: String?
        get() = _sourceFileRef?.value()
        set(newValue) {
          _sourceFileRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var begin: Int? = null

      public var end: Int? = null

      public var semantic: Semantic? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): Annotation =
        Annotation(
          freezeList(path),
          _sourceFileRef,
          begin,
          end,
          semantic,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: Annotation): Builder =
          Builder().also {
            it.path = msg.path
            it._sourceFileRef = msg._sourceFile
            it.begin = msg.begin
            it.end = msg.end
            it.semantic = msg.semantic
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<Annotation>() {
      @JvmStatic
      override fun deserialize(reader: Reader): Annotation {
        var path: ListBuilder<Int>? = null
        var sourceFile: Bytes? = null
        var begin: Int? = null
        var end: Int? = null
        var semantic: Semantic? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return Annotation(
                path?.build() ?: emptyList(),
                sourceFile?.let { LazyReference(it, StringConverter) },
                begin,
                end,
                semantic,
                UnknownFieldSet.from(unknownFields)
              )
            }

            8u -> {
              path =
                (path ?: listBuilder()).apply {
                  reader.readRepeated(false) {
                    add(reader.readInt32())
                  }
                }
            }

            10u -> {
              path =
                (path ?: listBuilder()).apply {
                  reader.readRepeated(true) {
                    add(reader.readInt32())
                  }
                }
            }

            18u -> {
              sourceFile = StringConverter.readValidatedBytes(reader)
            }

            24u -> {
              begin = reader.readInt32()
            }

            32u -> {
              end = reader.readInt32()
            }

            40u -> {
              semantic = reader.readEnum(Semantic)
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
      public operator fun invoke(dsl: Builder.() -> Unit): Annotation =
        Builder().apply(dsl).build()
    }

    /**
     * Represents the identified object's effect on the element in the original .proto file.
     */
    public sealed class Semantic(
      override val `value`: Int,
      override val name: String
    ) : Enum() {
      /**
       * There is no effect or the effect is indescribable.
       */
      public object NONE : Semantic(0, "NONE")

      /**
       * The element is set or otherwise mutated.
       */
      public object SET : Semantic(1, "SET")

      /**
       * An alias to the element is returned.
       */
      public object ALIAS : Semantic(2, "ALIAS")

      public class UNRECOGNIZED(
        `value`: Int
      ) : Semantic(value, "UNRECOGNIZED")

      public companion object Deserializer : EnumDeserializer<Semantic> {
        override fun deserialize(`value`: Int): Semantic =
          when (value) {
            0 -> NONE
            1 -> SET
            2 -> ALIAS
            else -> UNRECOGNIZED(value)
          }
      }
    }
  }
}
