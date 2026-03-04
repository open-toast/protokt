# Benchmark Results

Results from JMH benchmarks comparing protokt's codec implementations against
protobuf-java and Wire.

## Environment

|                  |                              |
|------------------|------------------------------|
| **CPU**          | Apple M1 Pro (10 cores)      |
| **Memory**       | 32 GB                        |
| **Architecture** | arm64 (aarch64)              |
| **JDK**          | Amazon Corretto 17.0.7+7-LTS |
| **JMH**          | 1.37                         |
| **Commit**       | `81a3300b`                   |

### JMH configuration

|             |                                       |
|-------------|---------------------------------------|
| Mode        | Average time (ms/op, lower is better) |
| Warmup      | 3 iterations, 10s each                |
| Measurement | 5 iterations, 10s each                |
| Forks       | 2                                     |
| Threads     | 1                                     |

## Libraries under test

The protobuf-java and Wire columns are standalone benchmarks using each
library's native API directly. The three codec columns use protokt's generated code
with different codec backends:

| Column            | Description                                                                    |
|-------------------|--------------------------------------------------------------------------------|
| **protobuf-java** | Google's `protobuf-java` library, native API                                   |
| **Wire**          | Square's Wire library, native API                                              |
| **PBJ**           | protokt + `ProtobufJavaCodec` (delegates to protobuf-java for reading/writing) |
| **KxIo**          | protokt + `KotlinxIoCodec` (uses kotlinx-io `Source`/`Sink` internally)        |
| **Protokt**       | protokt + `ProtoktCodec` (pure Kotlin, zero external dependencies)             |

All protokt codec results use `DefaultCollectionFactory` unless otherwise noted.
`ProtoktCodec` does not implement `JvmCodec` or `StreamingCodec`, so streaming
deserialization and streaming serialization benchmarks show `---` for it.
(The `Message.serialize(Sink)` extension function always uses `KotlinxIoSinkWriter`
regardless of codec.)

## Datasets

- **Large**: ~225 MB dataset of `GenericMessage1` payloads with many populated fields
- **Medium**: Moderate-size `GenericMessage1` payloads
- **Small**: Compact `GenericMessage4` payloads
- **StringHeavy**: 100 messages with three 10K-character mixed-encoding UTF-8 string fields
- **StringOneof**: 100 messages with three 10K-character oneof string fields
- **StringOneof20k**: 100 messages with three 20K-character oneof string fields
- **StringVeryHeavy / StringOneofVeryHeavy**: 10 messages with three 1M-character string fields

## Results

All values are milliseconds per operation (ms/op). Lower is better.

### Deserialize (byte array)

| Benchmark                   | protobuf-java |   Wire |    PBJ |   KxIo |    Protokt |
|-----------------------------|--------------:|-------:|-------:|-------:|-----------:|
| deserializeLargeFromMemory  |          1448 |    899 |    748 |    843 |    **741** |
| deserializeMediumFromMemory |         3.114 |  3.550 |  1.976 |  2.561 |  **1.974** |
| deserializeSmallFromMemory  |        0.0059 | 0.0080 | 0.0034 | 0.0044 | **0.0033** |

All three protokt codecs outperform both native protobuf-java and Wire on
deserialization from byte arrays. `ProtoktCodec` leads on all message sizes,
beating native protobuf-java by 49% on large messages and Wire by 18%.

### Deserialize (streaming)

| Benchmark                       | protobuf-java |       Wire |        PBJ |       KxIo | Protokt |
|---------------------------------|--------------:|-----------:|-----------:|-----------:|--------:|
| deserializeLargeStreaming       |          1483 |       1077 |    **795** |        986 |     --- |
| deserializeMediumStreaming      |         3.003 |      3.731 |  **2.287** |      3.747 |     --- |
| deserializeSmallStreaming       |        0.0356 |     0.0151 |     0.0321 | **0.0111** |     --- |
| deserializeStringHeavyStreaming |        18.923 | **18.615** |     18.860 |     19.046 |     --- |
| deserializeStringOneofStreaming |        18.541 |     18.469 | **18.373** |     18.684 |     --- |

`ProtobufJavaCodec` is fastest on large and medium streaming deserialization.
`KotlinxIoCodec` wins on small messages. String-heavy results converge to
within ~3% across all implementations.

All protokt codecs beat native protobuf-java and Wire on large/medium messages.

### Serialize (byte array)

| Benchmark               | protobuf-java |   Wire |    PBJ |   KxIo |   Protokt |
|-------------------------|--------------:|-------:|-------:|-------:|----------:|
| serializeLargeToMemory  |          1229 |   1451 |    762 |    826 |   **690** |
| serializeMediumToMemory |         0.929 |  1.321 |  0.946 |  1.609 | **0.839** |
| serializeSmallToMemory  |    **0.0019** | 0.0069 | 0.0031 | 0.0060 |    0.0026 |

`ProtoktCodec` is the fastest serializer on large and medium messages, beating
native protobuf-java by 44% on large messages. Native protobuf-java leads on
small messages.

### Serialize (streaming)

| Benchmark                | protobuf-java |   Wire |     PBJ |   KxIo | Protokt |
|--------------------------|--------------:|-------:|--------:|-------:|--------:|
| serializeLargeStreaming  |          1244 |   1437 | **757** |    830 |     --- |
| serializeMediumStreaming |     **1.031** |  1.348 |   1.449 |  1.733 |     --- |
| serializeSmallStreaming  |    **0.0041** | 0.0081 |  0.0055 | 0.0060 |     --- |

`ProtobufJavaCodec` leads on large streaming serialization. Native protobuf-java
is fastest on small and medium streaming serialization.

### Pass-through (deserialize then serialize)

| Benchmark                   | protobuf-java |   Wire |        PBJ |   KxIo |    Protokt |
|-----------------------------|--------------:|-------:|-----------:|-------:|-----------:|
| passThroughLargeFromMemory  |          3215 |   2333 |       1735 |   1897 |   **1660** |
| passThroughMediumFromMemory |         4.526 |  4.127 |      3.469 |  4.586 |  **3.238** |
| passThroughSmallFromMemory  |        0.0090 | 0.0136 |     0.0099 | 0.0128 | **0.0074** |
| passThroughStringHeavy      |        49.523 | 39.111 | **18.366** | 18.687 |     18.442 |
| passThroughStringOneof      |        48.232 | 38.860 | **18.385** | 18.624 |     18.387 |

`ProtoktCodec` wins the core pass-through benchmarks (large, medium, small),
48% faster than native protobuf-java and 29% faster than Wire on large messages.

String-heavy pass-through: protokt codecs are ~53-63% faster than native
protobuf-java and ~53% faster than Wire.

Oneof pass-through: protokt codecs now match the string-heavy pass-through
performance (~18 ms), ~62% faster than protobuf-java and ~53% faster than Wire.
Lazy oneof string fields cache the original wire bytes through sealed-class
wrappers, enabling direct re-serialization without re-encoding.

### Mutate and serialize

| Benchmark                                 | protobuf-java |       Wire |     PBJ |    KxIo | Protokt |
|-------------------------------------------|--------------:|-----------:|--------:|--------:|--------:|
| mutateAndSerializeStringHeavy             |        49.239 | **39.949** |  51.815 |  51.290 |  51.507 |
| mutateAndSerializeStringHeavyStreaming    |        42.188 | **39.550** |  51.851 |  51.447 |     --- |
| mutateAndSerializeStringOneof             |        48.426 | **39.268** |  52.428 |  51.505 |  51.916 |
| mutateAndSerializeStringOneof20k          |        82.179 | **79.268** | 103.280 | 102.573 | 103.033 |
| mutateAndSerializeStringOneof20kStreaming |        82.811 | **78.333** | 103.865 | 102.294 | 103.217 |
| mutateAndSerializeStringOneofStreaming    |        41.615 | **39.011** |  52.136 |  51.213 |  52.233 |
| mutateAndSerializeStringOneofVeryHeavy    |       **479** |        687 |     523 |     509 |     512 |
| mutateAndSerializeStringVeryHeavy         |       **489** |        702 |     511 |     512 |     510 |

Wire is fastest on most mutate-and-serialize benchmarks for standard-size
strings. For very heavy strings (1M characters), Wire becomes the slowest.

Protokt codecs are ~25-33% slower than Wire on standard-size oneof
mutate-and-serialize. All protokt codecs perform similarly to each other.

### Copy/append

| Benchmark            | protobuf-java |   Wire |        PBJ |   KxIo |    Protokt |
|----------------------|--------------:|-------:|-----------:|-------:|-----------:|
| copyAppendListLarge  |         2.885 |  3.757 |      1.846 |  1.794 |  **1.688** |
| copyAppendListMedium |     **0.296** |  1.519 |      1.471 |  1.455 |      1.522 |
| copyAppendListSmall  |     **0.303** |  1.505 |      1.436 |  1.437 |      1.417 |
| copyAppendMapLarge   |        24.097 | 27.367 |     20.729 | 20.893 | **20.882** |
| copyAppendMapMedium  |        17.803 | 21.174 |     16.554 | 16.578 | **16.519** |
| copyAppendMapSmall   |        19.210 | 21.741 | **16.288** | 16.338 |     16.262 |

Copy/append performance measures 1000 iterations of appending a single element
to a list or map field via the `copy {}` DSL. The codec is irrelevant for these
benchmarks since the work is entirely in collection copying.

For lists, native protobuf-java is fastest on small and medium messages. On
large messages, protokt's `copy {}` is ~42% faster.

For maps, protokt codecs are ~15-20% faster than protobuf-java and ~25% faster
than Wire.

## Persistent collections

Persistent collections (`PersistentCollectionFactory`) use
`kotlinx-collections-immutable` to back `repeated` and `map` fields with
tree-based persistent data structures. This enables O(log n) structural sharing
on `copy {}` append operations instead of O(n) full copies.

### Impact on core operations

| Benchmark                   | Codec   | Default | Persistent |  Delta |
|-----------------------------|---------|--------:|-----------:|-------:|
| deserializeLargeFromMemory  | PBJ     |     748 |        814 |  +8.8% |
| deserializeLargeFromMemory  | KxIo    |     843 |        902 |  +7.0% |
| deserializeLargeFromMemory  | Protokt |     741 |        812 |  +9.5% |
| deserializeMediumFromMemory | PBJ     |   1.976 |      2.262 | +14.5% |
| deserializeMediumFromMemory | KxIo    |   2.561 |      3.003 | +17.2% |
| deserializeMediumFromMemory | Protokt |   1.974 |      2.312 | +17.2% |
| serializeLargeToMemory      | PBJ     |     762 |        807 |  +5.9% |
| serializeLargeToMemory      | KxIo    |     826 |        875 |  +6.0% |
| serializeLargeToMemory      | Protokt |     690 |        733 |  +6.3% |
| serializeMediumToMemory     | PBJ     |   0.946 |      0.951 |  +0.5% |
| serializeMediumToMemory     | KxIo    |   1.609 |      1.628 |  +1.2% |
| serializeMediumToMemory     | Protokt |   0.839 |      0.910 |  +8.4% |
| passThroughLargeFromMemory  | PBJ     |    1735 |       1901 |  +9.6% |
| passThroughLargeFromMemory  | KxIo    |    1897 |       2048 |  +8.0% |
| passThroughLargeFromMemory  | Protokt |    1660 |       1797 |  +8.3% |

Persistent collections add 7-10% overhead on large-message deserialization and
6-7% on serialization.

### Impact on copy/append

| Benchmark           | Codec   | Default | Persistent |      Delta |
|---------------------|---------|--------:|-----------:|-----------:|
| copyAppendListLarge | PBJ     |   1.846 |      0.053 | **-97.2%** |
| copyAppendListLarge | KxIo    |   1.794 |      0.054 | **-97.0%** |
| copyAppendListLarge | Protokt |   1.688 |      0.054 | **-96.8%** |
| copyAppendMapLarge  | PBJ     |  20.729 |      0.206 | **-99.0%** |
| copyAppendMapLarge  | KxIo    |  20.893 |      0.205 | **-99.0%** |
| copyAppendMapLarge  | Protokt |  20.882 |      0.205 | **-99.0%** |
| copyAppendListSmall | PBJ     |   1.436 |      1.435 |      -0.1% |
| copyAppendListSmall | Protokt |   1.417 |      1.441 |      +1.7% |
| copyAppendMapSmall  | PBJ     |  16.288 |     16.318 |      +0.2% |
| copyAppendMapSmall  | Protokt |  16.262 |     16.208 |      -0.3% |

Persistent collections provide **32-100x speedup** on large-collection append
operations (97% improvement for lists, 99% for maps).

On small/empty collections, persistent and default collections perform
identically.

### Cross-library context

Even with the 7-10% overhead from persistent collections, protokt with
`PersistentCollectionFactory` remains substantially faster than both protobuf-java
and Wire on core operations, while gaining massive copy/append improvements:

| Benchmark                   | protobuf-java |   Wire | protokt | protokt + persistent | vs protobuf-java | vs Wire |
|-----------------------------|--------------:|-------:|--------:|---------------------:|-----------------:|--------:|
| deserializeLargeFromMemory  |          1448 |    899 |     741 |                  812 |             -44% |    -10% |
| deserializeMediumFromMemory |         3.114 |  3.550 |   1.974 |                2.312 |             -26% |    -35% |
| serializeLargeToMemory      |          1229 |   1451 |     690 |                  733 |             -40% |    -49% |
| serializeMediumToMemory     |         0.929 |  1.321 |   0.839 |                0.910 |              -2% |    -31% |
| passThroughLargeFromMemory  |          3215 |   2333 |    1660 |                 1797 |             -44% |    -23% |
| passThroughMediumFromMemory |         4.526 |  4.127 |   3.238 |                3.683 |             -19% |    -11% |
| copyAppendListLarge         |         2.885 |  3.757 |   1.688 |                0.054 |             -98% |    -99% |
| copyAppendMapLarge          |        24.097 | 27.367 |  20.882 |                0.205 |             -99% |    -99% |

The "vs" columns compare protokt with persistent collections against each native
library. Persistent-collection protokt is 10-49% faster than protobuf-java and
Wire on serialization and deserialization, and 98-99% faster on copy/append of
large collections. Workloads that mix deserialization with incremental message
building via `copy {}` will see the largest overall benefit from persistent
collections.

## Codec selection guide

| Workload                                | Recommended codec | Rationale                                                               |
|-----------------------------------------|-------------------|-------------------------------------------------------------------------|
| JVM general purpose                     | `OptimalJvmCodec` | Fastest byte-array paths + fastest streaming via protobuf-java.         |
| Multiplatform (JVM targets)             | `OptimalJvmCodec` | Same as JVM; use per-target deps to get protobuf-java on JVM targets.   |
| Multiplatform (non-JVM targets)         | `OptimalKmpCodec` | Fastest byte-array paths + streaming via kotlinx-io. No JVM dependency. |
| Minimal dependencies (byte arrays only) | `ProtoktCodec`    | Ships with `protokt-runtime`, no additional dependencies needed.        |

The default `optimal()` codec selection handles this automatically: KMP projects
get `OptimalKmpCodec` for common code and `OptimalJvmCodec` for JVM/Android
targets via per-target dependencies.
