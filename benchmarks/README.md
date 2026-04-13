# Benchmarks

See [RESULTS.md](RESULTS.md) for detailed benchmark results and analysis.

## Running

### kotlinx-benchmark (JVM + Native)

Run protokt benchmarks on the current platform via kotlinx-benchmark:

```
./gradlew :benchmarks:protokt-benchmarks:jvmBenchmark
./gradlew :benchmarks:protokt-benchmarks:macosArm64Benchmark
```

These use kotlinx-benchmark (JMH on JVM, native runtime on K/N) and run the
full `ProtoktMultiplatformBenchmarks` suite on both platforms for direct
comparison.

### JMH runner (JVM only)

Run benchmarks via the legacy JMH runner for protokt, protobuf-java, and Wire:

```
./gradlew :benchmarks:protokt-benchmarks:run
./gradlew :benchmarks:protobuf-java-benchmarks:run
./gradlew :benchmarks:wire-benchmarks:run
```

The JMH runner supports flags and parameter selection (see below). The
protobuf-java and Wire modules are JVM-only comparison benchmarks.

## Flags

Flags are passed via `--args` (JMH runner only):

| Flag | Description |
|------|-------------|
| `-i regex` | Include only benchmarks matching regex |
| `-e regex` | Exclude benchmarks matching regex |
| `-p name=value` | Set a JMH parameter |

When no `-i` flag is given, all benchmarks in the class are included.

## Parameters

protokt benchmarks accept these JMH parameters (set via `-p`):

| Parameter | Values | Default |
|-----------|--------|---------|
| `collectionFactory` | `protokt.v1.DefaultCollectionFactory`, `protokt.v1.PersistentCollectionFactory` | Both |
| `codec` | `protokt.v1.ProtobufJavaCodec`, `protokt.v1.KotlinxIoCodec`, `protokt.v1.ProtoktCodec`, `protokt.v1.OptimalKmpCodec`, `protokt.v1.OptimalJvmCodec` | All |

## Examples

Run a single benchmark method:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-i serializeSmall"
```

Exclude copy/append benchmarks:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-e .*copyAppend.*"
```

Pin a JMH parameter:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-p collectionFactory=protokt.v1.DefaultCollectionFactory"
```

Run with a specific codec:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-p codec=protokt.v1.ProtoktCodec"
```

Combine flags:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-i serialize -e .*String.*"
```
