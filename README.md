# protokt 

[![Github Actions](https://github.com/open-toast/protokt/actions/workflows/ci.yml/badge.svg)](https://github.com/open-toast/protokt/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.toasttab.protokt/protokt-runtime)](https://search.maven.org/artifact/com.toasttab.protokt/protokt-runtime)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/toasttab/protokt/protokt-gradle-plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.toasttab.protokt)

Protocol Buffer compiler and runtime for Kotlin.

Supports only version 3 of the Protocol Buffers language.

## Overview

### Features
- Idiomatic and concise [Kotlin builder DSL](#generated-code)
- Protokt-specific options: [non-null types (dangerous)](#non-null-fields),
[wrapper types](#wrapper-types),
[interface implementation](#interface-implementation),
and more
- Representation of the well-known types as Kotlin nullable types: `StringValue`
is represented as `String?`, etc.
- Multiplatform support for Kotlin JS (beta)
- (JVM) Built on Protobuf's Java library: usage of CodedInputStream and
CodedOutputStream for best performance
- (JS) Built on protobufJS for best performance
- gRPC [method descriptor and service descriptor generation](#grpc-code-generation)
for use with [grpc-java](#integrating-with-grpcs-java-api), 
[grpc-kotlin](#integrating-with-grpcs-kotlin-api), and
[grpc-node](#integrating-with-grpcs-nodejs-api) (experimental) (see examples  in [examples](examples))

### Not yet implemented

- Kotlin/Native support
- Protobuf JSON support 

### Compatibility

The Gradle plugin requires Java 8+ and Gradle 5.6+. It runs on recent versions of
MacOS, Linux, and Windows.

The runtime and generated code are compatible with Kotlin 1.8+, Java 8+, and Android 4.4+. 

## Usage

See examples in [testing](testing).

### Gradle

```kotlin
plugins {
    id("com.toasttab.protokt") version "<version>"
}
```

or

```kotlin
buildscript {
    dependencies {
        classpath("com.toasttab.protokt:protokt-gradle-plugin:<version>")
    }
}

apply(plugin = "com.toasttab.protokt")
```

This will automatically download and install protokt, apply the Google protobuf
plugin, and configure all the necessary boilerplate. By default it will also add
`protokt-core` to the `api` scope of the project. On the JVM you must explicitly
choose to depend on `protobuf-java` or `protobuf-javalite`:

```kotlin
dependencies {
    implementation("com.google.protobuf:protobuf-java:<version>")
}
```

or

```kotlin
dependencies {
    implementation("com.google.protobuf:protobuf-javalite:<version>")
}
```

If your project has no Java code you may run into the following error:

```
Execution failed for task ':compileJava'.
> error: no source files
```

To work around it, disable all `JavaCompile` tasks in the project:

```kotlin
tasks.withType<JavaCompile> {
    enabled = false
}
```

### Generated Code

Generated code is placed in `<buildDir>/generated/<sourceSet.name>/protokt`.

A simple example:

```protobuf
syntax = "proto3";

package toasttab.protokt.sample;

message Sample {
  string sample_field = 1;
}
```

will produce:

```kotlin
// Generated by protokt version 1.0.0-beta.2-SNAPSHOT. Do not modify.
// Source: protokt/v1/testing/test.proto
@file:Suppress("DEPRECATION")

package protokt.v1.toasttab.protokt.sample

import protokt.v1.AbstractDeserializer
import protokt.v1.AbstractMessage
import protokt.v1.BuilderDsl
import protokt.v1.GeneratedFileDescriptor
import protokt.v1.GeneratedMessage
import protokt.v1.GeneratedProperty
import protokt.v1.Reader
import protokt.v1.SizeCodecs.sizeOf
import protokt.v1.UnknownFieldSet
import protokt.v1.Writer
import protokt.v1.google.protobuf.Descriptor
import protokt.v1.google.protobuf.FileDescriptor
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.jvm.JvmStatic

@GeneratedMessage("toasttab.protokt.sample.Sample")
public class Sample private constructor(
    @GeneratedProperty(1)
    public val sampleField: String,
    public val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
    private val `$messageSize`: Int by lazy {
        var result = 0
        if (sampleField.isNotEmpty()) {
            result += sizeOf(10u) + sizeOf(sampleField)
        }
        result += unknownFields.size()
        result
    }

    override fun messageSize(): Int = `$messageSize`

    override fun serialize(writer: Writer) {
        if (sampleField.isNotEmpty()) {
            writer.writeTag(10u).write(sampleField)
        }
        writer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean =
        other is Sample &&
                other.sampleField == sampleField &&
                other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + sampleField.hashCode()
        return result
    }

    override fun toString(): String =
        "Sample(" +
                "sampleField=$sampleField" +
                if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

    public fun copy(builder: Builder.() -> Unit): Sample =
        Builder().apply {
            sampleField = this@Sample.sampleField
            unknownFields = this@Sample.unknownFields
            builder()
        }.build()

    @BuilderDsl
    public class Builder {
        public var sampleField: String = ""

        public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        public fun build(): Sample =
            Sample(
                sampleField,
                unknownFields
            )
    }

    public companion object Deserializer : AbstractDeserializer<Sample>() {
        @JvmStatic
        override fun deserialize(reader: Reader): Sample {
            var sampleField = ""
            var unknownFields: UnknownFieldSet.Builder? = null

            while (true) {
                when (reader.readTag()) {
                    0u -> return Sample(
                        sampleField,
                        UnknownFieldSet.from(unknownFields)
                    )
                    10u -> sampleField = reader.readString()
                    else ->
                        unknownFields =
                            (unknownFields ?: UnknownFieldSet.Builder()).also {
                                it.add(reader.readUnknown())
                            }
                }
            }
        }

        @JvmStatic
        public operator fun invoke(dsl: Builder.() -> Unit): Sample = Builder().apply(dsl).build()
    }
}

@GeneratedFileDescriptor
public object test_file_descriptor {
    public val descriptor: FileDescriptor by lazy {
        val descriptorData =
            arrayOf(
                "\nprotokt/v1/testing/test.prototoastta" +
                        "b.protokt.sample\"\nSample\nsample_fie" +
                        "ld (\tbproto3"
            )

        FileDescriptor.buildFrom(
            descriptorData,
            listOf()
        )
    }
}

public val Sample.Deserializer.descriptor: Descriptor
    get() = test_file_descriptor.descriptor.messageTypes[0]
```

Construct your protokt object like so:

```kotlin
Sample {
    sampleField = "some-string"
}
```

Why not expose a public constructor or use a data class? One of the design goals
of protocol buffers is that protobuf definitions can be modified in
backwards-compatible ways without breaking wire or API compatibility of existing
code. Using a DSL to construct the object emulates named arguments and allows
shuffling of protobuf fields within a definition without breaking code as would
happen for a standard constructor or method call.

The canonical `copy` method on data classes is emulated via a generated `copy`
method:

```kotlin
val sample = Sample { sampleField = "some-string" }

val sample2 = sample.copy { sampleField = "some-other-string" }
```

Assigning a Map or List in the DSL makes a copy of that collection to prevent
any escaping mutability of the provided collection. The Java protobuf
implementation takes a similar approach; it only exposes mutation methods on the
builder and not assignment. Mutating the builder does a similar copy operation.

## Runtime Notes

### Package

The Kotlin package of a generated file is the protobuf package prefixed with
`protokt.v1`. This scheme allows protokt-generated files to coexist on the
classpath with files generated by other compilers.

### Message

Each protokt message implements the `KtMessage` interface. `KtMessage` defines
the `serialize()` method and its overloads which can serialize to a byte array
or an `OutputStream`.

Each protokt message has a companion object `Deserializer` that implements the
`KtDeserializer` interface, which provides the `deserialize()` method and its
overloads to construct an instance of the message from a byte array, a Java
InputStream, or others.

### Enums

#### Representation

Protokt represents enum fields as sealed classes with an integer value and name.
Protobuf enums cannot be represented as Kotlin enum classes since Kotlin enum
classes are closed and cannot represent unknown values. The Protocol Buffers
specification requires that unknown enum values are preserved for
reserialization, so this compromise enables exhaustive case switching while
allowing representation of unknown values.

```kotlin
public sealed class PhoneType(
  override val `value`: Int,
  override val name: String
) : Enum() {
  public object MOBILE : PhoneType(0, "MOBILE")

  public object HOME : PhoneType(1, "HOME")

  public object WORK : PhoneType(2, "WORK")

  public class UNRECOGNIZED(
    `value`: Int
  ) : PhoneType(value, "UNRECOGNIZED")

  public companion object Deserializer : EnumReader<PhoneType> {
    override fun from(`value`: Int): PhoneType =
      when (value) {
        0 -> MOBILE
        1 -> HOME
        2 -> WORK
        else -> UNRECOGNIZED(value)
      }
  }
}
```

#### Naming

To keep enums ergonomic while promoting protobuf best practices, enums that have
all values
[prefixed with the enum type name](https://developers.google.com/protocol-buffers/docs/style#enums)
will have that prefix stripped in their Kotlin representations.

### Reflection

#### Descriptors

Protokt generates and embeds descriptors for protobuf files in its output by default. Generation can be disabled
while using the lite runtime:

```kotlin
protokt {
    generate {
        descriptors = false
    } 
}
```

#### Interop with `protobuf-java`

Protokt includes [utilities](protokt-reflect/src/jvmMain/kotlin/protokt/v1/google/protobuf/Messages.kt) to reflectively
(i.e., no-copy) convert a `protokt.v1.Message` to a `com.google.protobuf.Message`. Conversion requires that you specify
the RuntimeContext of your proto files. If you would like to scan your classpath for all known descriptors at runtime,
you may use Protokt's `GeneratedFileDescriptor` annotation [to do so](testing/interop/src/test/kotlin/protokt/v1/testing/RuntimeContextUtil.kt):

```kotlin
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import io.github.classgraph.ClassGraph
import protokt.v1.GeneratedFileDescriptor
import protokt.v1.google.protobuf.FileDescriptor
import protokt.v1.google.protobuf.RuntimeContext
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

fun getContextReflectively() =
    RuntimeContext(getDescriptors())

private fun getDescriptors() =
    ClassGraph()
        .enableAnnotationInfo()
        .scan()
        .use {
            it.getClassesWithAnnotation(GeneratedFileDescriptor::class.java)
                .map { info ->
                    @Suppress("UNCHECKED_CAST")
                    info.loadClass().kotlin as KClass<Any>
                }
        }
        .asSequence()
        .map { klassWithDescriptor ->
            klassWithDescriptor
                .declaredMemberProperties
                .single { it.returnType.classifier == FileDescriptor::class }
                .get(klassWithDescriptor.objectInstance!!) as FileDescriptor
        }
        .flatMap { it.toProtobufJavaDescriptor().messageTypes }
        .flatMap(::collectDescriptors)
        .asIterable()

private fun collectDescriptors(descriptor: Descriptors.Descriptor): Iterable<Descriptors.Descriptor> =
    listOf(descriptor) + descriptor.nestedTypes.flatMap(::collectDescriptors)

private fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
    Descriptors.FileDescriptor.buildFrom(
        DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
        dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
        true
    )
```

### Other Notes

- `optimize_for` is ignored.
- `repeated` fields are represented as Lists.
- `map` fields are represented as Maps.
- `oneof` fields are represented as subtypes of a sealed base class with a
single property.
- `bytes` fields are wrapped in the protokt `Bytes` class to ensure immutability
akin to `protobuf-java`'s `ByteString`.
- Protokt implements proto3's `optional`.

## Extensions

See extension options defined in
[protokt.proto](extensions/protokt-extensions-lite/src/main/proto/protokt/v1/protokt.proto).

See examples of each option in the [options](testing/options/src/main/proto)
project. All protokt-specific options require importing `protokt/v1/protokt.proto`
in the protocol file.

### Wrapper Types

Sometimes a field on a protobuf message corresponds to a concrete nonprimitive
type. In standard protobuf the user would be responsible for this extra
transformation, but the protokt wrapper type option allows specification of a
converter that will automatically encode and decode custom types to protobuf
types. Some standard types are implemented in [extensions](extensions).

Wrap a field by invoking the `(protokt.v1.property).wrap` option:

```protobuf
message WrapperMessage {
  google.protobuf.Timestamp instant = 1 [
    (protokt.v1.property).wrap = "java.time.Instant"
  ];
}
```

Converters implement the
[Converter](extensions/protokt-extensions-api/src/commonMain/kotlin/protokt/v1/Converter.kt)
interface:

```kotlin
interface Converter<ProtobufT : Any, KotlinT : Any> {
  val wrapper: KClass<KotlinT>

  val wrapped: KClass<ProtobufT>

  val acceptsDefaultValue
    get() = true

  fun wrap(unwrapped: ProtobufT): KotlinT

  fun unwrap(wrapped: KotlinT): ProtobufT
}
```

and protokt will reference the converter's methods to wrap and unwrap from
protobuf primitives:

```kotlin
object InstantConverter : AbstractConverter<Timestamp, Instant>() {
    override fun wrap(unwrapped: Timestamp): Instant =
        Instant.ofEpochSecond(unwrapped.seconds, unwrapped.nanos.toLong())

    override fun unwrap(wrapped: Instant) =
        Timestamp {
            seconds = wrapped.epochSecond
            nanos = wrapped.nano
        }
}
```

```kotlin
@GeneratedMessage("protokt.v1.testing.WrapperMessage")
public class WrapperMessage private constructor(
  @GeneratedProperty(1)
  public val instant: Instant?,
  public val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {
  private val `$messageSize`: Int by lazy {
    var result = 0
    if (instant != null) {
      result += sizeOf(10u) + sizeOf(InstantConverter.unwrap(instant))
    }
    result += unknownFields.size()
    result
  }

  override fun messageSize(): Int = `$messageSize`

  override fun serialize(writer: Writer) {
    if (instant != null) {
      writer.writeTag(10u).write(InstantConverter.unwrap(instant))
    }
    writer.writeUnknown(unknownFields)
  }

  override fun equals(other: Any?): Boolean =
    other is WrapperMessage &&
      other.instant == instant &&
      other.unknownFields == unknownFields

  override fun hashCode(): Int {
    var result = unknownFields.hashCode()
    result = 31 * result + instant.hashCode()
    return result
  }

  override fun toString(): String =
    "WrapperMessage(" +
      "instant=$instant" +
      if (unknownFields.isEmpty()) ")" else ", unknownFields=$unknownFields)"

  public fun copy(builder: Builder.() -> Unit): WrapperMessage =
    Builder().apply {
      instant = this@WrapperMessage.instant
      unknownFields = this@WrapperMessage.unknownFields
      builder()
    }.build()

  @BuilderDsl
  public class Builder {
    public var instant: Instant? = null

    public var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

    public fun build(): WrapperMessage =
      WrapperMessage(
        instant,
        unknownFields
      )
    }

  public companion object Deserializer : AbstractDeserializer<WrapperMessage>() {
    @JvmStatic
    override fun deserialize(reader: Reader): WrapperMessage {
      var instant: Instant? = null
      var unknownFields: UnknownFieldSet.Builder? = null

      while (true) {
        when (reader.readTag()) {
          0u -> return WrapperMessage(
            instant,
            UnknownFieldSet.from(unknownFields)
          )
          10u -> instant = InstantConverter.wrap(reader.readMessage(Timestamp))
          else ->
            unknownFields =
              (unknownFields ?: UnknownFieldSet.Builder()).also {
                it.add(reader.readUnknown())
              }
        }
      }
    }

    @JvmStatic
    public operator fun invoke(dsl: Builder.() -> Unit): WrapperMessage = Builder().apply(dsl).build()
  }
}
```

Each converter must be registered in a
`META-INF/services/protokt.v1.Converter`
classpath resource following the standard `ServiceLoader` convention. For
example, Google's [AutoService](https://github.com/google/auto/tree/master/service)
can register converters with an annotation:

```kotlin
@AutoService(Converter::class)
object InstantConverter : Converter<Instant, Timestamp> { ... }
```

Converters can also implement the `OptimizedSizeofConverter` interface adding
`sizeof()`, which allows them to optimize the calculation of the wrapper's size
rather than unwrap the object twice. For example, a UUID is always 16 bytes:

```kotlin
object UuidBytesConverter : OptimizedSizeOfConverter<UUID, Bytes> {
    override val wrapper = UUID::class

    override val wrapped = Bytes::class

    private val sizeOfProxy = ByteArray(16)

    override fun sizeOf(wrapped: UUID) =
        sizeOf(sizeOfProxy)

    override fun wrap(unwrapped: Bytes): UUID {
        val buf = unwrapped.asReadOnlyBuffer()

        require(buf.remaining() == 16) {
            "UUID source must have size 16; had ${buf.remaining()}"
        }

        return buf.run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): Bytes =
        Bytes.from(
            ByteBuffer.allocate(16)
                .putLong(wrapped.mostSignificantBits)
                .putLong(wrapped.leastSignificantBits)
               .array()
        )
```

Rather than convert a UUID to a byte array both for size calculation and for
serialization (which is what a naïve implementation would do), UuidConverter
always returns the size of a constant 16-byte array.

If the wrapper type is in the same package as the generated protobuf message,
then it does not need a fully-qualified name. Custom wrapper type converters can
be in the same project as protobuf types that reference them. In order to use any
wrapper type defined in `extensions`, the project must be included as a
dependency:

```kotlin
dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:<version>")
}
```

Wrapper types that wrap protobuf messages are nullable. For example,
`java.time.Instant` wraps the well-known type `google.protobuf.Timestamp`. They
can be made non-nullable by using the non-null option described below.

Wrapper types that wrap protobuf primitives, for example `java.util.UUID`
which wraps `bytes`, are nullable when they cannot wrap their wrapped type's 
default value. Converters must override `acceptsDefaultValue` to be `false` in 
these cases. For example, a UUID cannot wrap an empty byte array and each of
the following declarations will produce a nullable property:

```protobuf
bytes uuid = 1 [
  (protokt.v1.property).wrap = "java.util.UUID"
];

optional bytes optional_uuid = 2 [
  (protokt.v1.property).wrap = "java.util.UUID"
];

google.protobuf.BytesValue nullable_uuid = 3 [
  (protokt.v1.property).wrap = "java.util.UUID"
];
```

This behavior can be overridden with the [`non_null` option](#non-null-fields).

Wrapper types can be repeated:

```protobuf
repeated bytes uuid = 1 [
  (protokt.v1.property).wrap = "java.util.UUID"
];
```

And they can also be used for map keys and values:
```protobuf
map<string, protokt.ext.InetSocketAddress> map_string_socket_address = 1 [
  (protokt.v1.property).key_wrap = "StringBox",
  (protokt.v1.property).value_wrap = "java.net.InetSocketAddress"
];
```

Wrapper types should be immutable. If a wrapper type is defined in the same
package as generated protobuf message that uses it, then it does not need to
be referenced by its fully-qualified name and instead can be referenced by its
simple name, as done with `StringBox` in the map example above.

_N.b. Well-known type nullability is implemented with
[predefined wrapper types](protokt-core-lite/src/commonMain/kotlin/com/toasttab/protokt/v1)
for each message defined in
[wrappers.proto](https://github.com/protocolbuffers/protobuf/blob/main/src/google/protobuf/wrappers.proto)._

### Non-null fields
If a message has no meaning whatsoever when a particular non-scalar field is
missing, you can emulate proto2's `required` key word by using the
`(protokt.v1.property).non_null` option:

```protobuf
message Sample {}

message NonNullSampleMessage {
  Sample non_null_sample = 1 [
    (protokt.v1.property).non_null = true
  ];
}
```

Generated code will not have a nullable type, so the field can be referenced
without using Kotlin's `!!`.

Oneof fields can also be declared non-null:

```protobuf
message NonNullSampleMessage {
  oneof non_null_oneof {
    option (protokt.v1.oneof).non_null = true;

    string message = 1;
  }
}
```

Note that deserialization of a message with a non-nullable field will fail if the
message being decoded does not contain an instance of the required field.

This functionality will likely be removed.

### Interface implementation

#### Messages

To avoid the need to create domain-specific objects from protobuf messages you
can declare that a protobuf message implements a custom interface with
properties and default methods.

```kotlin
package com.protokt.sample

interface Model {
    val id: String
}
```

```protobuf
package protokt.sample;

message ImplementsSampleMessage {
  option (protokt.v1.class).implements = "Model";

  string id = 1;
}
```

Like wrapper types, if the implemented interface is in the same package as the
generated protobuf message that uses it, then it does not need to be referenced
by its fully-qualified name. Implemented interfaces cannot be used by protobuf
messages in the same project that defines them; the dependency must be declared
with `protoktExtensions` in `build.gradle`:

```groovy
dependencies {
    protoktExtensions(project(":api-project"))
}
```

Messages can also implement interfaces by delegation to one of their fields;
in this case the delegated interface need not live in a separate project, as
protokt requires no inspection of it:

```protobuf
message ImplementsWithDelegate {
  option (protokt.v1.class).implements = "Model2 by modelTwo";

  ImplementsModel2 model_two = 1 [
    (protokt.v1.property).non_null = true
  ];
}
```

Note that the `by` clause references the field by its lower camel case name.

#### Oneof Fields

Oneof fields can declare that they implement an interface with the 
`(protokt.v1.oneof).implements` option. Each possible field type of the oneof must
also implement the interface. This allows access of common properties without a
`when` statement that always ultimately extracts the same property.

Suppose you have a domain object MyObjectWithConfig that has a non-null configuration
that specifies a third-party server for communication. For flexibility, this
configuration will be modifiable by the server and versioned by a simple integer.
To hasten subsequent loading of the configuration, a client may save a resolved
version of the configuration with the same version and an additional field
storing an InetAddress representing the location of the server. Since the
server address may change over time, the client-resolved version of the config will
retain a copy of the original server copy. We can model this domain with protokt:

Given the Config interface:

```kotlin
package com.toasttab.example

interface Config {
    val version: Int
}
```

And protobuf definitions:

```protobuf
syntax = "proto3";

package toasttab.example;

import "protokt/v1/protokt.proto";

message MyObjectWithConfig {
  bytes id = 1 [
    (protokt.v1.property).wrap = "java.util.UUID"
  ];

  oneof Config {
    option (protokt.v1.oneof).non_null = true;
    option (protokt.v1.oneof).implements = "com.toasttab.example.Config";

    ServerSpecified server_specified = 2;
    ClientResolved client_resolved = 3;
  }
}

message ServerSpecified {
  option (protokt.v1.class).implements = "com.toasttab.example.Config";

  int32 version = 1;

  string server_registry = 2;
  string server_name = 3;
}

message ClientResolved {
  option (protokt.v1.class).implements = "com.toasttab.example.Config by config";

  ServerSpecified config = 1 [
    (protokt.v1.property).non_null = true
  ];

  bytes last_known_address = 2 [
    (protokt.v1.property).wrap = "java.net.InetAddress"
  ];
}
```

Protokt will generate:

```kotlin
@GeneratedMessage("toasttab.example.MyObjectWithConfig")
public class MyObjectWithConfig private constructor(
  @GeneratedProperty(1)
  public val id: UUID?,
  public val config: Config,
  public val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage() {

  // methods and builders omitted

  public sealed class Config : com.toasttab.example.Config {
    public data class ServerSpecified(
      @GeneratedProperty(2)
      public val serverSpecified: protokt.v1.toasttab.example.ServerSpecified
    ) : Config(), com.toasttab.example.Config by serverSpecified

    public data class ClientResolved(
      @GeneratedProperty(3)
      public val clientResolved: protokt.v1.toasttab.example.ClientResolved
    ) : Config(), com.toasttab.example.Config by clientResolved
  }
}

@GeneratedMessage("toasttab.example.ServerSpecified")
public class ServerSpecified private constructor(
  @GeneratedProperty(1)
  override val version: Int,
  @GeneratedProperty(2)
  public val serverRegistry: String,
  @GeneratedProperty(3)
  public val serverName: String,
  public val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage(), Config {

  // methods and builders omitted

}

@GeneratedMessage("toasttab.example.ClientResolved")
public class ClientResolved private constructor(
  @GeneratedProperty(1)
  public val config: ServerSpecified,
  @GeneratedProperty(2)
  public val lastKnownAddress: InetAddress?,
  public val unknownFields: UnknownFieldSet = UnknownFieldSet.empty()
) : AbstractMessage(), Config by config {

    // methods and builders omitted

}
```

A MyObjectWithConfig.Config instance can be queried for its version without
accessing the property via a `when` expression:

```kotlin
fun printVersion(config: MyObjectWithConfig.Config) {
    println(config.version)
}
```

This pattern is dangerous: since the oneof must be marked non-nullable, you
cannot compatibly add new implementing fields to a producer before a consumer
is updated with the new generated code. The old consumer will attempt to
deserialize the new field as an unknown field and the non-null assertion on the
oneof field during the constructor call will fail.

This functionality will likely be removed.

### BytesSlice

When reading messages that contain other serialized messages as `bytes` fields,
protokt can keep a reference to the originating byte array to prevent a large
copy operation on deserialization. This can be desirable when the wrapping
message is short-lived or a thin metadata shim and doesn't include much memory
overhead:

```protobuf
message SliceModel {
  int64 version = 1;

  bytes encoded_message = 2 [
    (protokt.v1.property).bytes_slice = true
  ];
}
```

## gRPC code generation

Protokt will generate variations of code for gRPC method and service descriptors
when the gRPC generation options are enabled:

```groovy
protokt {
    generate {
        grpcDescriptors = true
        grpcKotlinStubs = true
    } 
}
```

The options can be enabled independently of each other.

### Generated gRPC code

#### `grpcDescriptors`

Consider gRPC's canonical Health service:

```protobuf
syntax = "proto3";

package grpc.health.v1;

message HealthCheckRequest {
  string service = 1;
}

message HealthCheckResponse {
  enum ServingStatus {
    UNKNOWN = 0;
    SERVING = 1;
    NOT_SERVING = 2;
  }
  ServingStatus status = 1;
}

service Health {
  rpc Check(HealthCheckRequest) returns (HealthCheckResponse);
}
```

In addition to the request and response types, protokt will generate a service
descriptor and method descriptors for each method on the service:

```kotlin
public object HealthGrpc {
  public const val SERVICE_NAME: String = "grpc.health.v1.Health"

  private val _serviceDescriptor: GrpcServiceDescriptor by lazy {
    serviceDescriptorNewBuilder(SERVICE_NAME)
      .addMethod(_checkMethod)
      .setSchemaDescriptor(
        SchemaDescriptor(
          className = "protokt.v1.grpc.health.v1.Health",
          fileDescriptorClassName = "protokt.v1.grpc.health.v1.health_file_descriptor"
        )
      )
      .build()
  }

  private val _checkMethod: MethodDescriptor<HealthCheckRequest, HealthCheckResponse> by lazy {
    methodDescriptorNewBuilder<HealthCheckRequest, HealthCheckResponse>()
      .setType(UNARY)
      .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Check"))
      .setRequestMarshaller(KtMarshaller(HealthCheckRequest))
      .setResponseMarshaller(KtMarshaller(HealthCheckResponse))
      .build()
  }

  @JvmStatic
  public fun getServiceDescriptor(): GrpcServiceDescriptor = _serviceDescriptor

  @JvmStatic
  public fun getCheckMethod(): MethodDescriptor<HealthCheckRequest, HealthCheckResponse> = _checkMethod
}
```

Both grpc-java and grpc-kotlin expose server stubs for implementation via
abstract classes.

#### `grpcKotlinStubs` and gRPC's Kotlin API

Protokt uses `grpc-kotlin` to generate Kotlin coroutine-based stubs that compile
against protokt's generated types.

#### Integrating with gRPC's Java API

A gRPC service using grpc-java (and therefore using StreamObservers for
asynchronous communication):

```kotlin
abstract class HealthCheckService : BindableService {
    override fun bindService() =
        ServerServiceDefinition.builder(serviceDescriptor)
            .addMethod(checkMethod, asyncUnaryCall(::check))
            .build()

    open fun check(
        request: HealthCheckRequest,
        responseObserver: StreamObserver<HealthCheckResponse>
    ): Unit =
        throw UNIMPLEMENTED.asException()
}
```

Calling methods from a client:

```kotlin
fun checkHealth(): HealthCheckResponse =
    ClientCalls.blockingUnaryCall(
        channel.newCall(HealthGrpc.checkMethod, CallOptions.DEFAULT),
        HealthCheckRequest { service = "foo" }
    )
```

#### Integrating with gRPC's NodeJS API

Protokt generates complete server and client stub implementations for use with NodeJS.
The generated implementations are nearly the same as those generated by grpc-kotlin and
are supported by an analogous runtime library in ServerCalls and ClientCalls objects.

These implementations are alpha-quality and for demonstration only. External contributions 
to harden the implementation are welcome. They use the same `grpcDescriptors` and
`grpcKotlinStubs` plugin options to control code generation.

## IntelliJ integration

If IntelliJ doesn't automatically detect the generated files as source files,
you may be missing the `idea` plugin. Apply the `idea` plugin to your Gradle
project:

```groovy
plugins {
  id 'idea'
}
```

## Command line code generation

```sh
protokt % ./gradlew assemble

protokt % protoc \
    --plugin=protoc-gen-custom=protokt-codegen/build/install/protoc-gen-protokt/bin/protoc-gen-protokt \
    --custom_out=<output-directory> \
    -I<path-to-proto-file-containing-directory> \
    -Iprotokt-runtime/src/main/resources \
    <path-to-proto-file>.proto
```

For example, to generate files in `protokt/foo` from a file called `test.proto`
located at `protokt/test.proto`:

```sh
protokt % protoc \
    --plugin=protoc-gen-custom=protokt-codegen/build/install/protoc-gen-protokt/bin/protoc-gen-protokt \
    --custom_out=foo \
    -I. \
    -Iprotokt-runtime/src/main/resources \
    test.proto
```

## Contribution

Community contributions are welcome. See the 
[contribution guidelines](CONTRIBUTING.md) and the project
[code of conduct](CODE-OF-CONDUCT.md).

To enable rapid development of the code generator, the protobuf conformance
tests have been compiled and included in the `testing` project. They run on Mac
OS 10.14+ and Ubuntu 16.04 x86-64 as part of normal Gradle builds.

When integration testing the Gradle plugin, note that after changing the plugin
and republishing it to the integration repository, `./gradlew clean` is needed
to trigger regeneration of the protobuf files with the fresh plugin.

## Acknowledgements

### Authors

[Ben Gordon](mailto:ben.gordon@toasttab.com),
[Andrew Parmet](mailto:andrew.parmet@toasttab.com),
[Oleg Golberg](mailto:ogolberg@toasttab.com),
[Frank Moda](mailto:frank@toasttab.com),
[Romey Sklar](mailto:jerome.sklar@toasttab.com), and
[everyone in the commit history](../../commits/main).

Thanks to the Google Kotlin team for their
[Kotlin API Design](https://github.com/lowasser/protobuf/blob/master/kotlin-design.md)
which inspired the DSL builder implemented in this library.
