# protokt 

[![CircleCI](https://circleci.com/gh/open-toast/protokt.svg?style=svg)](https://circleci.com/gh/open-toast/protokt)
[![Maven Central](https://img.shields.io/maven-central/v/com.toasttab.protokt/protokt-runtime)](https://search.maven.org/artifact/com.toasttab.protokt/protokt-codegen)

Protocol Buffer compiler and runtime for Kotlin.

Supports only version 3 of the Protocol Buffers language.

#### Features
- Idiomatic and concise Kotlin builder DSL
- Protokt-specific options: non-null types, wrapper types, interface implementation,
and more
- Representation of the well-known types as Kotlin nullable types: `StringValue`
is represented as `String?`, etc.
- Built on Protobuf's Java library: usage of CodedInputStream and
CodedOutputStream for best performance

#### Not yet implemented

- Support for gRPC service and client generation (*In progress!*)
- Kotlin native support (**Looking for contributors**)
- Kotlin JS support (**Looking for contributors**)
- Protobuf JSON support 

See examples in [testing](testing).

#### Compatibility

The Gradle plugin requires Java 8+, Gradle 5.6+ and runs on recent versions of
MacOS, Linux, and Windows.

The runtime and generated code are compatible with Java 8+ and Android 4.4+. 

### Usage

#### Gradle

```groovy
plugins {
  id "com.toasttab.protokt" version "<version>"
}
```
or

```groovy
buildscript {
  dependencies {
    classpath 'com.toasttab.protokt:protokt-gradle-plugin:<version>'
  }
}

apply plugin: 'com.toasttab.protokt'
```

This will automatically download and install protokt, apply the Google protobuf
plugin, and configure all the necessary boilerplate. By default it will also add
`protokt-core` to the api scope of the project. You must explicitly choose to
depend on `protobuf-java` or `protobuf-javalite`:

```groovy
dependencies {
  'com.google.protobuf:protobuf-java:<version>'
}
```

or

```groovy
dependencies {
  'com.google.protobuf:protobuf-javalite:<version>'
}
```

If your project is pure Kotlin you may run into the following error:

```
Execution failed for task ':compileJava'.
> error: no source files
```

To work around it, disable all `JavaCompile` tasks in the project:

```groovy
tasks.withType(JavaCompile) {
  enabled = false
}
```

or:

```groovy
compileJava.enabled = false
```

#### Generated Code

Generated code is placed in `<buildDir>/generated-sources/main/protokt`.

A simple example:

```proto
syntax = "proto3";

package com.protokt.sample;

message Sample {
  string sample_field = 1;
}
```

will produce:

```kotlin
/*
 * Generated by protokt version <version>. Do not modify.
 * Source: com/protokt/sample/sample.proto
 */

package com.protokt.sample

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.Unknown
import com.toasttab.protokt.rt.copyMap
import com.toasttab.protokt.rt.finishMap
import com.toasttab.protokt.rt.processUnknown
import com.toasttab.protokt.rt.sizeof

@KtGeneratedMessage("com.protokt.sample.Sample")
class Sample
private constructor(
    val sampleField: String,
    val unknown: Map<Int, Unknown> = emptyMap()
) : KtMessage {
    override val messageSize by lazy { sizeof() }

    override fun serialize(serializer: KtMessageSerializer) {
        if (sampleField.isNotEmpty()) {
            serializer.write(Tag(10)).write(sampleField)
        }
        if (unknown.isNotEmpty()) {
            serializer.writeUnknown(unknown)
        }
    }

    private fun sizeof(): Int {
        var res = 0
        if (sampleField.isNotEmpty()) {
            res += sizeof(Tag(1)) + sizeof(sampleField)
        }
        res += unknown.values.sumBy { it.sizeof() }
        return res
    }

    override fun equals(other: Any?): Boolean =
        other is Sample &&
            other.sampleField == sampleField &&
            other.unknown == unknown

    override fun hashCode(): Int {
        var result = unknown.hashCode()
        result = 31 * result + sampleField.hashCode()
        return result
    }

    override fun toString(): String =
        "Sample(" +
            "sampleField=$sampleField, " +
            "unknown=$unknown)"

    fun copy(dsl: SampleDsl.() -> Unit) =
        Sample {
            sampleField = this@Sample.sampleField
            unknown = this@Sample.unknown
            dsl()
        }

    class SampleDsl {
        var sampleField = ""
        var unknown: Map<Int, Unknown> = emptyMap()
            set(newValue) { field = copyMap(newValue) }

        fun build() =
            Sample(
                sampleField,
                unknown
            )
    }

    companion object Deserializer : KtDeserializer<Sample>, (SampleDsl.() -> Unit) -> Sample {
        override fun deserialize(deserializer: KtMessageDeserializer): Sample {
            var sampleField = ""
            var unknown: MutableMap<Int, Unknown>? = null

            while (true) {
                when (deserializer.readTag()) {
                    0 ->
                        return Sample(
                            sampleField,
                            finishMap(unknown)
                        )
                    10 -> sampleField = 
                        deserializer.readString()
                    else -> unknown =
                        (unknown ?: mutableMapOf()).also {
                            processUnknown(deserializer, it)
                        }
                }
            }
        }

        override fun invoke(dsl: SampleDsl.() -> Unit) =
            SampleDsl().apply(dsl).build()
    }
}
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

### Runtime Notes

#### Package

By default, the Kotlin package of a generated file is the same as the protobuf
package. Second in precedence is the declared `java_package` option, which can
be disabled by settings the `respectJavaPackage` property to `false` in the
Gradle configuration block:

```groovy
protokt {
  respectJavaPackage = false
}
```

Disabling this option at the code-generator level can be helpful when migrating
a codebase already using third-party protobuf with the `java_package` option in
use to prevent duplicate class issues.

Highest precedence is given to `(protokt.file).kotlin_package` option:

```proto
syntax = "proto3";

import "protokt/protokt.proto";

package com.example;

option (protokt.file).kotlin_package = "com.package";

...
```

#### Message

Each protokt message implements the `KtMessage` interface. `KtMessage` defines
the `serialize()` method and its overloads which can serialize to a byte array,
a `KtMessageSerializer`, or an `OutputStream`.

Each protokt message has a companion object `Deserializer` that implements the
`KtDeserializer` interface, which provides the `deserialize()` method and its
overloads to construct an instance of the message from a byte array, a Java
InputStream, or others.

#### Enums

Protokt represents enum fields as sealed classes with an integer value and name.
Protobuf enums cannot be represented as Kotlin enum classes since Kotlin enum
classes are closed and cannot represent unknown values. The Protocol Buffers
specifications requires that unknown enum values are preserved for
reserialization, so this compromise enables exhaustive case switching while
allowing representation of unknown values.

```kotlin
sealed class PhoneType(
    override val value: Int,
    override val name: String
) : KtEnum {
    override fun equals(other: Any?) =
        other is PhoneType && other.value == value

    override fun hashCode() =
        value

    override fun toString() =
        name

    object MOBILE : PhoneType(0, "MOBILE")

    object HOME : PhoneType(1, "HOME")

    object WORK : PhoneType(2, "WORK")

    class UNRECOGNIZED(value: Int) : PhoneType(value, "UNRECOGNIZED")

    companion object Deserializer : KtEnumDeserializer<PhoneType> {
        override fun from(value: Int) =
            when (value) {
                0 -> MOBILE
                1 -> HOME
                2 -> WORK
                else -> UNRECOGNIZED(value)
            }
    }
}
```

#### Other Notes

- `optimize_for` is ignored.
- `repeated` fields are represented as Lists.
- `map` fields are represented as Maps.
- `oneof` fields are represented as subtypes of a sealed base class with a
single property.
- `bytes` fields are wrapped in the protokt `Bytes` class to ensure immutability
akin to protobuf-java's `ByteString`.

### Extensions

See examples of each option in the [options](testing/options/src/main/proto)
module. All protokt-specific options require importing `protokt/protokt.proto`
in the protocol file.

#### Wrapper Types

Sometimes a field on a protobuf message corresponds to a concrete nonprimitive
type. In standard protobuf the user would be responsible for this extra
transformation, but the protokt wrapper type option allows specification of a
converter that will automatically encode and decode custom types to protobuf
types. Some standard types are implemented in [extensions](extensions).

Wrap a field by invoking the `(protokt.property).wrap` option:

```proto
message WrapperMessage {
  google.protobuf.Timestamp instant = 1 [
    (protokt.property).wrap = "java.time.Instant"
  ];
}
```

Converters implement the
[Converter](extensions/protokt-extensions-api/src/main/kotlin/com/toasttab/protokt/ext/Converter.kt)
interface:

```kotlin
interface Converter<S : Any, T : Any> {
    val wrapper: KClass<S>

    val wrapped: KClass<T>

    fun wrap(unwrapped: T): S

    fun unwrap(wrapped: S): T
}
```

and protokt will reference the converter's methods to wrap and unwrap from
protobuf primitives:

```kotlin
object InstantConverter : Converter<Instant, Timestamp> {
    override val wrapper = Instant::class

    override val wrapped = Timestamp::class

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
class WrapperModel
private constructor(
    val instant: java.time.Instant?,
    ...
) : KtMessage {
    ...
    override fun serialize(serializer: KtMessageSerializer) {
        if (instant != null) {
            serializer.write(Tag(42)).write(InstantConverter.unwrap(instant))
        }
        ...
    }

    override fun deserialize(deserializer: KtMessageDeserializer): WrapperModel {
        var instant: java.time.Instant? = null
        ...
        while (true) {
            when (deserializer.readTag()) {
                0 ->
                    return WrapperModel(
                        instant,
                        ...
                    )
                8 -> instant =
                    InstantConverter.wrap(deserializer.readMessage(com.toasttab.protokt.Timestamp))
                ...
            }
        }
    }
}
```

Each converter must be registered in a
`META-INF/services/com.toasttab.protokt.ext.Converter`
classpath resource following the standard `ServiceLoader` convention.

Converters can also implement the `OptimizedSizeofConverter` interface adding
`sizeof()`, which allows them to optimize the calculation of the wrapper's size
rather than unwrap the object twice. For example, a UUID is always 16 bytes:

```kotlin
object UuidConverter : OptimizedSizeofConverter<UUID, ByteArray> {
    override val wrapper = UUID::class

    override val wrapped = ByteArray::class

    private val sizeofProxy = ByteArray(16)

    override fun sizeof(wrapped: UUID) =
        sizeof(sizeofProxy)

    override fun wrap(unwrapped: ByteArray): UUID {
        require(unwrapped.size == 16) {
            "UUID source must have size 16; had ${unwrapped.size}"
        }

        return ByteBuffer.wrap(unwrapped)
            .run { UUID(long, long) }
    }

    override fun unwrap(wrapped: UUID): ByteArray =
        ByteBuffer.allocate(16)
            .putLong(wrapped.mostSignificantBits)
            .putLong(wrapped.leastSignificantBits)
            .array()
}
```

Rather than convert a UUID to a byte array both for size calculation and for
serialization (which is what a naïve implementation would do), UuidConverter
always returns the size of a constant 16-byte array.

If the wrapper type is in the same package as the generated protobuf message,
then it does not need a fully-qualified name. Custom wrapper type converters can
be in the same module as protobuf types that reference them. In order to use any
wrapper type defined in `extensions`, the module must be included as a
dependency:

```groovy
dependencies {
  implementation 'com.toasttab.protokt:protokt-extensions:<version>'
}
```

Wrapper types that wrap protobuf messages are nullable. For example,
`java.time.Instant` wraps the well-known type `google.protobuf.Timestamp`. They
can be made non-nullable by using the non-null option described below.

Wrapper types that wrap protobuf primitives, for example `java.util.UUID`
which wraps `bytes`, are not nullable and may present malformed inputs to
converters when absent in deserialization. It is up to the converter to
determine what behavior should be in these cases. To represent nullable
primitive wrappers use well-known types. For example for a nullable UUID:

```proto
google.protobuf.BytesValue uuid = 1 [
  (protokt.property).wrap = "java.util.UUID"
];
```

Wrapper types can be repeated:

```proto
repeated bytes uuid = 1 [
  (protokt.property).wrap = "java.util.UUID"
];
```

Wrapper types should be immutable.

#### Nonnull fields
If there is a message that has no meaning whatsoever when a particular field is missing, you
can emulate proto2's `required` key word by using the `(protokt.property).non_null` option:

```proto
message Sample {}

message NonNullSampleMessage {
  Sample non_null_sample = 1 [
    (protokt.property).non_null = true
  ];
}
```

Generated code will not have a nullable type so the field can be referenced without using
Kotlin's `!!`.

Oneof fields can also be declared non-null:

```proto
message NonNullSampleMessage {
  oneof non_null_oneof {
    option (protokt.oneof).non_null = true;

    string message = 2;
  }
}
```

Note that deserialization of a message with a non-nullable field will fail if the
message being decoded does not contain an instance of the required field.

#### Interface implementation

##### Messages

To avoid the need to create domain-specific objects from protobuf messages you
can declare that a protobuf message implements a custom interface with
properties and default methods.

```kotlin
package com.protokt.sample

interface Model {
    val id: String
}
```

```proto
package com.protokt.sample;

message ImplementsSampleMessage {
  option (protokt.class).implements = "Model";

  string id = 1;
}
```

If the wrapper interface is in the same package as the generated protobuf
message, then it does not need a fully-qualified name. Wrapper interfaces cannot
be used by protobuf messages in the same module that defines them; the
dependency must be declared with`protoktExtensions` in `build.gradle`:

```groovy
dependencies {
  protoktExtensions project(':api-module')
}
```

Messages can also implement interfaces by delegating to one of their fields;
in this case the delegated interface need not live in a separate module, as
protokt requires no inspection of the implemented interface:

```proto
message ImplementsWithDelegate {
  option (protokt.class).implements = "Model2 by modelTwo";

  ImplementsModel2 model_two = 1 [
    (protokt.property).non_null = true
  ];
}
```

Note that the `by` clause references the field by its lower camel case name.

##### Oneof Fields

Oneof fields can declare that they implement an interface with the 
`(protokt.oneof).implements` option. Each possible field type of the oneof must
also implement the interface. This allows access of common properties without a
`when` statement that always ultimately extracts the same property.

Suppose you have a domain object MyDomainObject that has a non-null configuration
that specifies a third-party server for communication. For flexibility, this
configuration will be modifiable by the server and versioned by a simple integer.
To hasten subsequent loading of the configuration, a client may save a resolved
version of the configuration with the same version and an additional field
storing the InetAddress representing location of the server. Since the
InetAddress may change over time, the client-resolved version of the config will
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

package com.toasttab.example;

import "protokt/protokt.proto";

message MyObjectWithConfig {
  bytes id = 1 [
    (protokt.property).non_null = true,
    (protokt.property).wrap = "java.util.UUID"
  ];

  oneof Config {
    option (protokt.oneof).non_null = true;
    option (protokt.oneof).implements = "Config";

    ServerSpecified server_updated = 2;
    ClientResolved client_resolved = 3;
  }
}

message ServerSpecified {
  option (protokt.class).implements = "Config";

  int32 version = 1;

  string server_registry = 2;
  string server_name = 3;
}

message ClientResolved {
  option (protokt.class).implements = "Config by config";

  ServerSpecified config = 2 [
    (protokt.property).non_null = true
  ];

  bytes last_known_address = 3 [
    (protokt.property).wrap = "java.net.InetAddress"
  ];
}
```

Protokt will generate:

```kotlin
@KtGeneratedMessage("com.toasttab.example.MyObjectWithConfig")
class MyObjectWithConfig
private constructor(
    val id: UUID,
    val config: Config,
    val unknown: Map<Int, Unknown> = emptyMap()
) : KtMessage {
    sealed class Config : com.toasttab.example.Config {
        data class ServerUpdated(
            val serverSpecified: ServerSpecified
        ) : Config(), com.toasttab.example.Config by serverUpdated 

        data class ClientResolved(
            val clientResolved: com.toasttab.example.ClientResolved
        ) : Config(), com.toasttab.example.Config by clientResolved 
    }

    ...
}

@KtGeneratedMessage("com.toasttab.example.ServerSpecified")
class ServerSpecified
private constructor(
    override val version: Int,
    val serverRegistry: String,
    val serverName: String,
    val unknown: Map<Int, Unknown> = emptyMap()
) : KtMessage, Config {

    ...
}

@KtGeneratedMessage("com.toasttab.example.ClientResolved")
class ClientResolved
private constructor(
    val config: ServerSpecified,
    val lastKnownAddress: InetAddress,
    val unknown: Map<Int, Unknown> = emptyMap()
) : KtMessage, Config by config {
    
    ...
}
```

A MyDomainObject.Config instance can be queried for its version without
accessing the property via a `when` expression:

```kotlin
fun printVersion(config: MyDomainObject.Config) {
    println(config.version)
}
```
#### BytesSlice

When reading messages that contain other serialized messages as `bytes` fields,
protokt can keep a reference to the originating byte array to prevent a large
copy operation on deserialization. This can be desirable when the wrapping
message is a thin metadata shim and doesn't include much memory overhead:

```proto
message SliceModel {
  int64 version = 1;

  bytes encoded_message = 2 [
    (protokt.property).bytes_slice = true
  ];
}
```

#### IntelliJ integration

If IntelliJ doesn't automatically detect the generated files as source files,
you may be missing the `idea` plugin. Apply the `idea` plugin to your Gradle
project:

```groovy
plugins {
  id 'idea'
}
```

#### Command line code generation

```bash
protokt$ ./gradlew assemble

protokt$ protoc \
    --plugin=protoc-gen-custom=protokt-codegen/build/install/protoc-gen-protokt/bin/protoc-gen-protokt \
    --custom_out=<output-directory> \
    -I<path-to-proto-file-containing-directory> \
    -Iprotokt-runtime/src/main/resources \
    <path-to-proto-file>.proto
```

For example, to generate files in `protokt/foo` from a file called `test.proto`
located at `protokt/test.proto`:

```bash
protokt$ protoc \
    --plugin=protoc-gen-custom=protokt-codegen/build/install/protoc-gen-protokt/bin/protoc-gen-protokt \
    --custom_out=foo \
    -I. \
    -Iprotokt-runtime/src/main/resources \
    test.proto
```

### Contribution
To enable rapid development of the code generator, the protobuf conformance
tests have been compiled and included in the `testing` project. They run on Mac
OS 10.14+ and Ubuntu 16.04 x86-64 as part of normal Gradle builds.

When integration testing the Gradle plugin, note that after changing the plugin
and republishing it to the integration repository, `./gradlew clean` is needed
to trigger regeneration of the protobuf files with the fresh plugin.

### Acknowledgements

#### Authors

[Ben Gordon](mailto:ben.gordon@toasttab.com),
[Andrew Parmet](mailto:andrew.parmet@toasttab.com),
[Oleg Golberg](mailto:ogolberg@toasttab.com),
[Patty Neckowicz](mailto:pneckowicz@toasttab.com),
[Frank Moda](mailto:frank@toasttab.com) and
[everyone in the commit history](../../commits/master).

Thanks to the Google Kotlin team for their
[Kotlin API Design](https://github.com/lowasser/protobuf/blob/master/kotlin-design.md)
which inspired the DSL builder implemented in this library.
