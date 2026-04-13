# Benchmarks

See [RESULTS.md](RESULTS.md) for detailed benchmark results and analysis.

## Running

All benchmarks use [kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark),
which delegates to JMH on JVM and uses its own runtime on Kotlin/Native.

```
./gradlew :benchmarks:protokt-benchmarks:jvmBenchmark
./gradlew :benchmarks:protokt-benchmarks:macosArm64Benchmark
./gradlew :benchmarks:protobuf-java-benchmarks:benchmark
./gradlew :benchmarks:wire-benchmarks:benchmark
```

All modules share the same default configuration (3 warmup iterations,
5 measurement iterations, 10s each, 2 forks, average time in ms/op).

## Gradle properties

Override defaults at invocation time via `-P`:

| Property | Description | Default |
|----------|-------------|---------|
| `benchmarkInclude` | Regex to include matching benchmarks | all |
| `benchmarkExclude` | Regex to exclude matching benchmarks | none |
| `benchmarkParam` | Comma-separated `name=value` pairs for `@Param` fields | all values |
| `benchmarkWarmups` | Warmup iterations | 3 |
| `benchmarkIterations` | Measurement iterations | 5 |
| `benchmarkForks` | JVM forks | 2 |

## Examples

Run only serialization benchmarks:

```
./gradlew :benchmarks:protokt-benchmarks:jvmBenchmark -PbenchmarkInclude=.*serialize.*
```

Run with a specific codec:

```
./gradlew :benchmarks:protokt-benchmarks:jvmBenchmark -PbenchmarkParam=codec=protokt.v1.ProtoktCodec
```

Quick smoke test (1 warmup, 1 iteration, 1 fork):

```
./gradlew :benchmarks:protokt-benchmarks:jvmBenchmark -PbenchmarkWarmups=1 -PbenchmarkIterations=1 -PbenchmarkForks=1
```

## Parameters

protokt benchmarks accept `@Param`-annotated properties:

| Parameter | Targets | Values |
|-----------|---------|--------|
| `collectionFactory` | JVM + Native | `protokt.v1.DefaultCollectionFactory`, `protokt.v1.PersistentCollectionFactory` |
| `codec` | JVM only | `protokt.v1.ProtobufJavaCodec`, `protokt.v1.KotlinxIoCodec`, `protokt.v1.ProtoktCodec`, `protokt.v1.OptimalKmpCodec`, `protokt.v1.OptimalJvmCodec` |

Native benchmarks always use `ProtoktCodec`. Collection factory selection works
on native via direct override (no env var or system property needed).
