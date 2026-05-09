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

package protokt.v1.google.protobuf.compiler

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
import protokt.v1.google.protobuf.FileDescriptorProto
import protokt.v1.google.protobuf.GeneratedCodeInfo
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.OptIn
import kotlin.String
import kotlin.Suppress
import kotlin.ULong
import kotlin.Unit
import kotlin.collections.List
import kotlin.jvm.JvmStatic

/**
 * The version number of protocol compiler.
 */
@GeneratedMessage("google.protobuf.compiler.Version")
public class Version private constructor(
  @GeneratedProperty(1)
  public val major: Int?,
  @GeneratedProperty(2)
  public val minor: Int?,
  @GeneratedProperty(3)
  public val patch: Int?,
  private val _suffix: LazyReference<Bytes, String>?,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (major != null) {
      result += sizeOf(8u) + sizeOf(major)
    }
    if (minor != null) {
      result += sizeOf(16u) + sizeOf(minor)
    }
    if (patch != null) {
      result += sizeOf(24u) + sizeOf(patch)
    }
    if (_suffix != null) {
      result += sizeOf(34u) + sizeOf(_suffix.wireValue())
    }
    result += unknownFields.size()
    result
  }

  /**
   * A suffix for alpha, beta or rc release, e.g., "alpha-1", "rc2". It should be empty for mainline stable releases.
   */
  @GeneratedProperty(4)
  public val suffix: String?
    get() = _suffix?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (major != null) {
      writer.writeTag(8u).write(major)
    }
    if (minor != null) {
      writer.writeTag(16u).write(minor)
    }
    if (patch != null) {
      writer.writeTag(24u).write(patch)
    }
    if (_suffix != null) {
      writer.writeTag(34u).write(_suffix.wireValue())
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is Version &&
      other.major == this.major &&
      other.minor == this.minor &&
      other.patch == this.patch &&
      other.suffix == this.suffix &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + major.hashCode()
    result = 31 * result + minor.hashCode()
    result = 31 * result + patch.hashCode()
    result = 31 * result + suffix.hashCode()
    return result
  }

  override fun toString(): String =
    "Version(" +
      "major=$major, " +
      "minor=$minor, " +
      "patch=$patch, " +
      "suffix=$suffix" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): Version =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var major: Int? = null

    public var minor: Int? = null

    public var patch: Int? = null

    private var _suffixRef: LazyReference<Bytes, String>? = null

    public var suffix: String?
      get() = _suffixRef?.value()
      set(newValue) {
        _suffixRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): Version =
      Version(
        major,
        minor,
        patch,
        _suffixRef,
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: Version): Builder =
        Builder().also {
          it.major = msg.major
          it.minor = msg.minor
          it.patch = msg.patch
          it._suffixRef = msg._suffix
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<Version>() {
    @JvmStatic
    override fun deserialize(reader: Reader): Version {
      var major: Int? = null
      var minor: Int? = null
      var patch: Int? = null
      var suffix: Bytes? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return Version(
              major,
              minor,
              patch,
              suffix?.let { LazyReference(it, StringConverter) },
              UnknownFieldSet.from(unknownFields)
            )
          }

          8u -> {
            major = reader.readInt32()
          }

          16u -> {
            minor = reader.readInt32()
          }

          24u -> {
            patch = reader.readInt32()
          }

          34u -> {
            suffix = StringConverter.readValidatedBytes(reader)
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
    public operator fun invoke(dsl: Builder.() -> Unit): Version =
      Builder().apply(dsl).build()
  }
}

/**
 * An encoded CodeGeneratorRequest is written to the plugin's stdin.
 */
@GeneratedMessage("google.protobuf.compiler.CodeGeneratorRequest")
public class CodeGeneratorRequest private constructor(
  /**
   * The .proto files that were explicitly listed on the command-line.  The code generator should generate code only for these files.  Each file's descriptor will be included in proto_file, below.
   */
  @GeneratedProperty(1)
  public val fileToGenerate: List<String>,
  private val _parameter: LazyReference<Bytes, String>?,
  /**
   * The version number of protocol compiler.
   */
  @GeneratedProperty(3)
  public val compilerVersion: Version?,
  /**
   * FileDescriptorProtos for all files in files_to_generate and everything they import.  The files will appear in topological order, so each file appears before any file that imports it.
   *
   *  Note: the files listed in files_to_generate will include runtime-retention options only, but all other files will include source-retention options. The source_file_descriptors field below is available in case you need source-retention options for files_to_generate.
   *
   *  protoc guarantees that all proto_files will be written after the fields above, even though this is not technically guaranteed by the protobuf wire format.  This theoretically could allow a plugin to stream in the FileDescriptorProtos and handle them one by one rather than read the entire set into memory at once.  However, as of this writing, this is not similarly optimized on protoc's end -- it will store all fields in memory at once before sending them to the plugin.
   *
   *  Type names of fields and extensions in the FileDescriptorProto are always fully qualified.
   */
  @GeneratedProperty(15)
  public val protoFile: List<FileDescriptorProto>,
  /**
   * File descriptors with all options, including source-retention options. These descriptors are only provided for the files listed in files_to_generate.
   */
  @GeneratedProperty(17)
  public val sourceFileDescriptors: List<FileDescriptorProto>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (fileToGenerate.isNotEmpty()) {
      result +=
        @Suppress("UNCHECKED_CAST")
        (fileToGenerate as LazyConvertingList<Bytes, Any>).let { list ->
          (sizeOf(10u) * list.size) +
            run {
              var sum = 0
              for (i in list.indices) sum += sizeOf(list.wireGet(i))
              sum
            }
        }
    }
    if (_parameter != null) {
      result += sizeOf(18u) + sizeOf(_parameter.wireValue())
    }
    if (compilerVersion != null) {
      result += sizeOf(26u) + sizeOf(compilerVersion)
    }
    if (protoFile.isNotEmpty()) {
      result += (sizeOf(122u) * protoFile.size) + protoFile.sumOf { sizeOf(it) }
    }
    if (sourceFileDescriptors.isNotEmpty()) {
      result += (sizeOf(138u) * sourceFileDescriptors.size) + sourceFileDescriptors.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  /**
   * The generator parameter passed on the command-line.
   */
  @GeneratedProperty(2)
  public val parameter: String?
    get() = _parameter?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (fileToGenerate.isNotEmpty()) {
      @Suppress("UNCHECKED_CAST")
      (fileToGenerate as LazyConvertingList<Bytes, Any>).wireForEach { writer.writeTag(10u).write(it) }
    }
    if (_parameter != null) {
      writer.writeTag(18u).write(_parameter.wireValue())
    }
    if (compilerVersion != null) {
      writer.writeTag(26u).write(compilerVersion)
    }
    protoFile.forEach { writer.writeTag(122u).write(it) }
    sourceFileDescriptors.forEach { writer.writeTag(138u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is CodeGeneratorRequest &&
      other.fileToGenerate == this.fileToGenerate &&
      other.parameter == this.parameter &&
      other.compilerVersion == this.compilerVersion &&
      other.protoFile == this.protoFile &&
      other.sourceFileDescriptors == this.sourceFileDescriptors &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + fileToGenerate.hashCode()
    result = 31 * result + parameter.hashCode()
    result = 31 * result + compilerVersion.hashCode()
    result = 31 * result + protoFile.hashCode()
    result = 31 * result + sourceFileDescriptors.hashCode()
    return result
  }

  override fun toString(): String =
    "CodeGeneratorRequest(" +
      "fileToGenerate=$fileToGenerate, " +
      "parameter=$parameter, " +
      "compilerVersion=$compilerVersion, " +
      "protoFile=$protoFile, " +
      "sourceFileDescriptors=$sourceFileDescriptors" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): CodeGeneratorRequest =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    public var fileToGenerate: List<String> = emptyList()
      set(newValue) {
        field = if (newValue is LazyConvertingList<*, *>) newValue else LazyConvertingList.fromKotlin(newValue, StringConverter)
      }

    private var _parameterRef: LazyReference<Bytes, String>? = null

    public var parameter: String?
      get() = _parameterRef?.value()
      set(newValue) {
        _parameterRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var compilerVersion: Version? = null

    public var protoFile: List<FileDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var sourceFileDescriptors: List<FileDescriptorProto> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): CodeGeneratorRequest =
      CodeGeneratorRequest(
        fileToGenerate,
        _parameterRef,
        compilerVersion,
        freezeList(protoFile),
        freezeList(sourceFileDescriptors),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: CodeGeneratorRequest): Builder =
        Builder().also {
          it.fileToGenerate = msg.fileToGenerate
          it._parameterRef = msg._parameter
          it.compilerVersion = msg.compilerVersion
          it.protoFile = msg.protoFile
          it.sourceFileDescriptors = msg.sourceFileDescriptors
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<CodeGeneratorRequest>() {
    @JvmStatic
    override fun deserialize(reader: Reader): CodeGeneratorRequest {
      var fileToGenerate: ListBuilder<Any?>? = null
      var parameter: Bytes? = null
      var compilerVersion: Version? = null
      var protoFile: ListBuilder<FileDescriptorProto>? = null
      var sourceFileDescriptors: ListBuilder<FileDescriptorProto>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return CodeGeneratorRequest(
              fileToGenerate?.build()?.let { LazyConvertingList<Bytes, String>(it, StringConverter) } ?: emptyList(),
              parameter?.let { LazyReference(it, StringConverter) },
              compilerVersion,
              protoFile?.build() ?: emptyList(),
              sourceFileDescriptors?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            fileToGenerate =
              (fileToGenerate ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(LazyReference(StringConverter.readValidatedBytes(reader), StringConverter))
                }
              }
          }

          18u -> {
            parameter = StringConverter.readValidatedBytes(reader)
          }

          26u -> {
            compilerVersion = reader.readMessage(Version)
          }

          122u -> {
            protoFile =
              (protoFile ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(FileDescriptorProto))
                }
              }
          }

          138u -> {
            sourceFileDescriptors =
              (sourceFileDescriptors ?: listBuilder()).apply {
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
    public operator fun invoke(dsl: Builder.() -> Unit): CodeGeneratorRequest =
      Builder().apply(dsl).build()
  }
}

/**
 * The plugin writes an encoded CodeGeneratorResponse to stdout.
 */
@GeneratedMessage("google.protobuf.compiler.CodeGeneratorResponse")
public class CodeGeneratorResponse private constructor(
  private val _error: LazyReference<Bytes, String>?,
  /**
   * A bitmask of supported features that the code generator supports. This is a bitwise "or" of values from the Feature enum.
   */
  @GeneratedProperty(2)
  public val supportedFeatures: ULong?,
  /**
   * The minimum edition this plugin supports.  This will be treated as an Edition enum, but we want to allow unknown values.  It should be specified according the edition enum value, *not* the edition number.  Only takes effect for plugins that have FEATURE_SUPPORTS_EDITIONS set.
   */
  @GeneratedProperty(3)
  public val minimumEdition: Int?,
  /**
   * The maximum edition this plugin supports.  This will be treated as an Edition enum, but we want to allow unknown values.  It should be specified according the edition enum value, *not* the edition number.  Only takes effect for plugins that have FEATURE_SUPPORTS_EDITIONS set.
   */
  @GeneratedProperty(4)
  public val maximumEdition: Int?,
  @GeneratedProperty(15)
  public val `file`: List<File>,
  override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val __serializedSize: Int by lazy {
    var result = 0
    if (_error != null) {
      result += sizeOf(10u) + sizeOf(_error.wireValue())
    }
    if (supportedFeatures != null) {
      result += sizeOf(16u) + sizeOf(supportedFeatures)
    }
    if (minimumEdition != null) {
      result += sizeOf(24u) + sizeOf(minimumEdition)
    }
    if (maximumEdition != null) {
      result += sizeOf(32u) + sizeOf(maximumEdition)
    }
    if (`file`.isNotEmpty()) {
      result += (sizeOf(122u) * `file`.size) + `file`.sumOf { sizeOf(it) }
    }
    result += unknownFields.size()
    result
  }

  /**
   * Error message.  If non-empty, code generation failed.  The plugin process should exit with status code zero even if it reports an error in this way.
   *
   *  This should be used to indicate errors in .proto files which prevent the code generator from generating correct code.  Errors which indicate a problem in protoc itself -- such as the input CodeGeneratorRequest being unparseable -- should be reported by writing a message to stderr and exiting with a non-zero status code.
   */
  @GeneratedProperty(1)
  public val error: String?
    get() = _error?.value()

  override fun serializedSize(): Int =
    __serializedSize

  override fun serialize(writer: Writer) {
    if (_error != null) {
      writer.writeTag(10u).write(_error.wireValue())
    }
    if (supportedFeatures != null) {
      writer.writeTag(16u).writeUInt64(supportedFeatures)
    }
    if (minimumEdition != null) {
      writer.writeTag(24u).write(minimumEdition)
    }
    if (maximumEdition != null) {
      writer.writeTag(32u).write(maximumEdition)
    }
    `file`.forEach { writer.writeTag(122u).write(it) }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is CodeGeneratorResponse &&
      other.error == this.error &&
      other.supportedFeatures == this.supportedFeatures &&
      other.minimumEdition == this.minimumEdition &&
      other.maximumEdition == this.maximumEdition &&
      other.`file` == this.`file` &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + error.hashCode()
    result = 31 * result + supportedFeatures.hashCode()
    result = 31 * result + minimumEdition.hashCode()
    result = 31 * result + maximumEdition.hashCode()
    result = 31 * result + `file`.hashCode()
    return result
  }

  override fun toString(): String =
    "CodeGeneratorResponse(" +
      "error=$error, " +
      "supportedFeatures=$supportedFeatures, " +
      "minimumEdition=$minimumEdition, " +
      "maximumEdition=$maximumEdition, " +
      "`file`=$`file`" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun toBuilder(): Builder =
    Builder.from(this)

  public fun copy(builder: Builder.() -> Unit): CodeGeneratorResponse =
    toBuilder().apply { builder() }.build()

  @BuilderDsl
  public class Builder : BuilderScope {
    private var _errorRef: LazyReference<Bytes, String>? = null

    public var error: String?
      get() = _errorRef?.value()
      set(newValue) {
        _errorRef = newValue?.let { LazyReference(it, StringConverter) }
      }

    public var supportedFeatures: ULong? = null

    public var minimumEdition: Int? = null

    public var maximumEdition: Int? = null

    public var `file`: List<File> = emptyList()
      set(newValue) {
        field = freezeList(newValue)
      }

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): CodeGeneratorResponse =
      CodeGeneratorResponse(
        _errorRef,
        supportedFeatures,
        minimumEdition,
        maximumEdition,
        freezeList(`file`),
        unknownFields
      )

    public companion object Factory {
      @OnlyForUseByGeneratedProtoCode
      internal fun from(msg: CodeGeneratorResponse): Builder =
        Builder().also {
          it._errorRef = msg._error
          it.supportedFeatures = msg.supportedFeatures
          it.minimumEdition = msg.minimumEdition
          it.maximumEdition = msg.maximumEdition
          it.`file` = msg.`file`
          it.unknownFields = msg.unknownFields
        }
    }
  }

  public companion object Deserializer : AbstractDeserializer<CodeGeneratorResponse>() {
    @JvmStatic
    override fun deserialize(reader: Reader): CodeGeneratorResponse {
      var error: Bytes? = null
      var supportedFeatures: ULong? = null
      var minimumEdition: Int? = null
      var maximumEdition: Int? = null
      var `file`: ListBuilder<File>? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> {
            return CodeGeneratorResponse(
              error?.let { LazyReference(it, StringConverter) },
              supportedFeatures,
              minimumEdition,
              maximumEdition,
              `file`?.build() ?: emptyList(),
              UnknownFieldSet.from(unknownFields)
            )
          }

          10u -> {
            error = StringConverter.readValidatedBytes(reader)
          }

          16u -> {
            supportedFeatures = reader.readUInt64()
          }

          24u -> {
            minimumEdition = reader.readInt32()
          }

          32u -> {
            maximumEdition = reader.readInt32()
          }

          122u -> {
            `file` =
              (`file` ?: listBuilder()).apply {
                reader.readRepeated(false) {
                  add(reader.readMessage(File))
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
    public operator fun invoke(dsl: Builder.() -> Unit): CodeGeneratorResponse =
      Builder().apply(dsl).build()
  }

  /**
   * Sync with code_generator.h.
   */
  public sealed class Feature(
    override val `value`: Int,
    override val name: String
  ) : Enum() {
    public object NONE : Feature(0, "NONE")

    public object PROTO3_OPTIONAL : Feature(1, "PROTO3_OPTIONAL")

    public object SUPPORTS_EDITIONS : Feature(2, "SUPPORTS_EDITIONS")

    public class UNRECOGNIZED(
      `value`: Int
    ) : Feature(value, "UNRECOGNIZED")

    public companion object Deserializer : EnumDeserializer<Feature> {
      override fun deserialize(`value`: Int): Feature =
        when (value) {
          0 -> NONE
          1 -> PROTO3_OPTIONAL
          2 -> SUPPORTS_EDITIONS
          else -> UNRECOGNIZED(value)
        }
    }
  }

  /**
   * Represents a single generated file.
   */
  @GeneratedMessage("google.protobuf.compiler.CodeGeneratorResponse.File")
  public class File private constructor(
    private val _name: LazyReference<Bytes, String>?,
    private val _insertionPoint: LazyReference<Bytes, String>?,
    private val _content: LazyReference<Bytes, String>?,
    /**
     * Information describing the file content being inserted. If an insertion point is used, this information will be appropriately offset and inserted into the code generation metadata for the generated files.
     */
    @GeneratedProperty(16)
    public val generatedCodeInfo: GeneratedCodeInfo?,
    override val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
  ) : AbstractMessage() {
    private val __serializedSize: Int by lazy {
      var result = 0
      if (_name != null) {
        result += sizeOf(10u) + sizeOf(_name.wireValue())
      }
      if (_insertionPoint != null) {
        result += sizeOf(18u) + sizeOf(_insertionPoint.wireValue())
      }
      if (_content != null) {
        result += sizeOf(122u) + sizeOf(_content.wireValue())
      }
      if (generatedCodeInfo != null) {
        result += sizeOf(130u) + sizeOf(generatedCodeInfo)
      }
      result += unknownFields.size()
      result
    }

    /**
     * The file name, relative to the output directory.  The name must not contain "." or ".." components and must be relative, not be absolute (so, the file cannot lie outside the output directory).  "/" must be used as the path separator, not "\".
     *
     *  If the name is omitted, the content will be appended to the previous file.  This allows the generator to break large files into small chunks, and allows the generated text to be streamed back to protoc so that large files need not reside completely in memory at one time.  Note that as of this writing protoc does not optimize for this -- it will read the entire CodeGeneratorResponse before writing files to disk.
     */
    @GeneratedProperty(1)
    public val name: String?
      get() = _name?.value()

    /**
     * If non-empty, indicates that the named file should already exist, and the content here is to be inserted into that file at a defined insertion point.  This feature allows a code generator to extend the output produced by another code generator.  The original generator may provide insertion points by placing special annotations in the file that look like:   @@protoc_insertion_point(NAME) The annotation can have arbitrary text before and after it on the line, which allows it to be placed in a comment.  NAME should be replaced with an identifier naming the point -- this is what other generators will use as the insertion_point.  Code inserted at this point will be placed immediately above the line containing the insertion point (thus multiple insertions to the same point will come out in the order they were added). The double-@ is intended to make it unlikely that the generated code could contain things that look like insertion points by accident.
     *
     *  For example, the C++ code generator places the following line in the .pb.h files that it generates:   // @@protoc_insertion_point(namespace_scope) This line appears within the scope of the file's package namespace, but outside of any particular class.  Another plugin can then specify the insertion_point "namespace_scope" to generate additional classes or other declarations that should be placed in this scope.
     *
     *  Note that if the line containing the insertion point begins with whitespace, the same whitespace will be added to every line of the inserted text.  This is useful for languages like Python, where indentation matters.  In these languages, the insertion point comment should be indented the same amount as any inserted code will need to be in order to work correctly in that context.
     *
     *  The code generator that generates the initial file and the one which inserts into it must both run as part of a single invocation of protoc. Code generators are executed in the order in which they appear on the command line.
     *
     *  If |insertion_point| is present, |name| must also be present.
     */
    @GeneratedProperty(2)
    public val insertionPoint: String?
      get() = _insertionPoint?.value()

    /**
     * The file contents.
     */
    @GeneratedProperty(15)
    public val content: String?
      get() = _content?.value()

    override fun serializedSize(): Int =
      __serializedSize

    override fun serialize(writer: Writer) {
      if (_name != null) {
        writer.writeTag(10u).write(_name.wireValue())
      }
      if (_insertionPoint != null) {
        writer.writeTag(18u).write(_insertionPoint.wireValue())
      }
      if (_content != null) {
        writer.writeTag(122u).write(_content.wireValue())
      }
      if (generatedCodeInfo != null) {
        writer.writeTag(130u).write(generatedCodeInfo)
      }
      writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
      other is File &&
        other.name == this.name &&
        other.insertionPoint == this.insertionPoint &&
        other.content == this.content &&
        other.generatedCodeInfo == this.generatedCodeInfo &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
      var result = unknownFields.hashCode()
      result = 31 * result + name.hashCode()
      result = 31 * result + insertionPoint.hashCode()
      result = 31 * result + content.hashCode()
      result = 31 * result + generatedCodeInfo.hashCode()
      return result
    }

    override fun toString(): String =
      "File(" +
        "name=$name, " +
        "insertionPoint=$insertionPoint, " +
        "content=$content, " +
        "generatedCodeInfo=$generatedCodeInfo" +
        if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun toBuilder(): Builder =
      Builder.from(this)

    public fun copy(builder: Builder.() -> Unit): File =
      toBuilder().apply { builder() }.build()

    @BuilderDsl
    public class Builder : BuilderScope {
      private var _nameRef: LazyReference<Bytes, String>? = null

      public var name: String?
        get() = _nameRef?.value()
        set(newValue) {
          _nameRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      private var _insertionPointRef: LazyReference<Bytes, String>? = null

      public var insertionPoint: String?
        get() = _insertionPointRef?.value()
        set(newValue) {
          _insertionPointRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      private var _contentRef: LazyReference<Bytes, String>? = null

      public var content: String?
        get() = _contentRef?.value()
        set(newValue) {
          _contentRef = newValue?.let { LazyReference(it, StringConverter) }
        }

      public var generatedCodeInfo: GeneratedCodeInfo? = null

      public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

      public fun build(): File =
        File(
          _nameRef,
          _insertionPointRef,
          _contentRef,
          generatedCodeInfo,
          unknownFields
        )

      public companion object Factory {
        @OnlyForUseByGeneratedProtoCode
        internal fun from(msg: File): Builder =
          Builder().also {
            it._nameRef = msg._name
            it._insertionPointRef = msg._insertionPoint
            it._contentRef = msg._content
            it.generatedCodeInfo = msg.generatedCodeInfo
            it.unknownFields = msg.unknownFields
          }
      }
    }

    public companion object Deserializer : AbstractDeserializer<File>() {
      @JvmStatic
      override fun deserialize(reader: Reader): File {
        var name: Bytes? = null
        var insertionPoint: Bytes? = null
        var content: Bytes? = null
        var generatedCodeInfo: GeneratedCodeInfo? = null
        var unknownFields: UnknownFieldSet.Builder? = null

        while (true) {
          when (reader.readTag()) {
            0u -> {
              return File(
                name?.let { LazyReference(it, StringConverter) },
                insertionPoint?.let { LazyReference(it, StringConverter) },
                content?.let { LazyReference(it, StringConverter) },
                generatedCodeInfo,
                UnknownFieldSet.from(unknownFields)
              )
            }

            10u -> {
              name = StringConverter.readValidatedBytes(reader)
            }

            18u -> {
              insertionPoint = StringConverter.readValidatedBytes(reader)
            }

            122u -> {
              content = StringConverter.readValidatedBytes(reader)
            }

            130u -> {
              generatedCodeInfo = reader.readMessage(GeneratedCodeInfo)
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
      public operator fun invoke(dsl: Builder.() -> Unit): File =
        Builder().apply(dsl).build()
    }
  }
}
