# Benchmark Results

Results from kotlinx-benchmark benchmarks comparing protokt's codec
implementations against protobuf-java and Wire on JVM and Kotlin/Native.

## Environment

|                  |                               |
|------------------|-------------------------------|
| **CPU**          | Apple M1 Pro (10 cores)       |
| **Memory**       | 32 GB                         |
| **Architecture** | arm64 (aarch64)               |
| **JDK**          | Amazon Corretto 17.0.18+9-LTS |
| **JMH**          | 1.37                          |
| **Commit**       | `03b54f50`                    |

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
- **StringMap**: 1000 iterations of a message with a `map<string, string>` field containing 100 entries
- **StringRepeated**: 1000 iterations of a message with a `repeated string` field containing 100 entries

## JVM Results

All values are milliseconds per operation (ms/op). Lower is better.

### Deserialize (byte array)

| Benchmark                   | protobuf-java |   Wire |    PBJ |   KxIo | Protokt |
|-----------------------------|--------------:|-------:|-------:|-------:|--------:|
| deserializeLargeFromMemory  |          1466 |    873 |    827 |    898 |     816 |
| deserializeMediumFromMemory |         3.006 |  3.055 |  2.067 |  2.371 |   1.895 |
| deserializeSmallFromMemory  |        0.0062 | 0.0088 | 0.0035 | 0.0044 |  0.0034 |
| deserializeStringMap        |         7.152 |  7.403 |  6.947 |  7.117 |   7.035 |
| deserializeStringRepeated   |         6.549 |  6.588 |  6.440 |  6.661 |   6.475 |

All three protokt codecs outperform protobuf-java on deserialization from byte
arrays. `ProtoktCodec` leads on large, medium, and small messages; PBJ leads on
string-collection benchmarks. Wire wins large-message deserialization.

### Deserialize (streaming)

| Benchmark                       | protobuf-java |   Wire |    PBJ |   KxIo | Protokt |
|---------------------------------|--------------:|-------:|-------:|-------:|--------:|
| deserializeLargeStreaming       |          1485 |    992 |    824 |    966 |     --- |
| deserializeMediumStreaming      |         3.227 |  3.819 |  2.226 |  2.901 |     --- |
| deserializeSmallStreaming       |        0.0323 | 0.0171 | 0.0312 | 0.0107 |     --- |
| deserializeStringHeavyStreaming |          18.4 |   18.6 |   16.8 |   16.9 |     --- |
| deserializeStringOneofStreaming |          18.9 |   18.5 |   16.9 |   17.0 |     --- |

`ProtobufJavaCodec` is fastest on large and medium streaming deserialization,
44% faster than native protobuf-java and 17% faster than Wire on large messages.

### Serialize (byte array)

| Benchmark               | protobuf-java |   Wire |    PBJ |   KxIo | Protokt |
|-------------------------|--------------:|-------:|-------:|-------:|--------:|
| serializeLargeToMemory  |          1238 |   1493 |    141 |    233 |     144 |
| serializeMediumToMemory |        0.8981 |  1.114 | 0.7597 |  1.251 |  0.7345 |
| serializeSmallToMemory  |        0.0017 | 0.0070 | 0.0038 | 0.0059 |  0.0028 |

`ProtobufJavaCodec` leads on large messages at 141 ms, 89% faster than
protobuf-java and 91% faster than Wire. `ProtoktCodec` is within noise of PBJ
on large and leads on medium.

### Serialize (streaming)

| Benchmark                | protobuf-java |   Wire |    PBJ |   KxIo | Protokt |
|--------------------------|--------------:|-------:|-------:|-------:|--------:|
| serializeLargeStreaming  |          1258 |   1437 |    184 |    217 |     197 |
| serializeMediumStreaming |         1.032 |  1.162 |  1.189 |  1.262 |   1.201 |
| serializeSmallStreaming  |        0.0040 | 0.0079 | 0.0047 | 0.0049 |  0.0043 |

`ProtobufJavaCodec` leads on large streaming serialization at 184 ms, 85% faster
than native protobuf-java and 87% faster than Wire. Native protobuf-java retains
the edge on small and medium streaming serialization.

### Pass-through (deserialize then serialize)

| Benchmark                   | protobuf-java |   Wire |    PBJ |   KxIo | Protokt |
|-----------------------------|--------------:|-------:|-------:|-------:|--------:|
| passThroughLargeFromMemory  |          3200 |   2308 |    995 |   1145 |     990 |
| passThroughMediumFromMemory |         4.616 |  4.090 |  3.328 |  4.231 |   2.873 |
| passThroughSmallFromMemory  |        0.0107 | 0.0141 | 0.0103 | 0.0127 |  0.0078 |
| passThroughStringHeavy      |          49.3 |   40.4 |   18.8 |   18.8 |    18.7 |
| passThroughStringOneof      |          49.2 |   39.9 |   18.6 |   18.8 |    18.6 |
| passThroughStringMap        |          20.9 |   15.9 |  7.818 |  8.117 |   7.648 |
| passThroughStringRepeated   |          17.4 |   14.3 |  6.954 |  7.270 |   6.807 |

`ProtoktCodec` wins the core pass-through benchmarks (large, medium, small),
69% faster than protobuf-java and 57% faster than Wire on large messages.

String-heavy and string-collection pass-through: all three protokt codecs
are substantially faster than both native libraries. The three protokt codecs
are within noise of each other on these workloads.

### Mutate and serialize

| Benchmark                                 | protobuf-java | Wire |  PBJ | KxIo | Protokt |
|-------------------------------------------|--------------:|-----:|-----:|-----:|--------:|
| mutateAndSerializeStringHeavy             |          49.2 | 39.9 | 51.7 | 51.7 |    52.1 |
| mutateAndSerializeStringHeavyStreaming    |          42.3 | 41.5 | 51.6 | 51.7 |    53.0 |
| mutateAndSerializeStringOneof             |          49.0 | 40.7 | 51.2 | 51.6 |    51.8 |
| mutateAndSerializeStringOneofStreaming    |          42.1 | 42.4 | 51.8 | 51.4 |    52.4 |
| mutateAndSerializeStringOneof20k          |          83.8 | 84.4 |  104 |  103 |     104 |
| mutateAndSerializeStringOneof20kStreaming |          83.3 | 81.2 |  104 |  103 |     106 |
| mutateAndSerializeStringOneofVeryHeavy    |           490 |  709 |  517 |  520 |     508 |
| mutateAndSerializeStringVeryHeavy         |           489 |  739 |  516 |  514 |     507 |

Wire is fastest on most mutate-and-serialize benchmarks for standard-size
strings. For very heavy strings (1M characters), Wire becomes the slowest and
protobuf-java leads. Protokt codecs are ~25% slower than Wire on standard-size
mutate-and-serialize.

### Copy/append

| Benchmark                 | protobuf-java |  Wire |    PBJ |   KxIo | Protokt |
|---------------------------|--------------:|------:|-------:|-------:|--------:|
| copyAppendListLarge       |         2.179 | 3.879 |  1.666 |  1.688 |   1.710 |
| copyAppendListMedium      |        0.2672 | 1.541 |  1.437 |  1.448 |   1.403 |
| copyAppendListSmall       |        0.2636 | 1.493 |  1.466 |  1.498 |   1.565 |
| copyAppendMapLarge        |          16.9 |  26.3 |   23.7 |   24.0 |    24.0 |
| copyAppendMapMedium       |          16.2 |  21.2 |   13.4 |   13.2 |    13.3 |
| copyAppendMapSmall        |          14.1 |  22.0 |   12.0 |   12.1 |    11.7 |
| copyAppendRepeatedString  |        0.5441 | 2.931 | 0.3043 | 0.2949 |  0.2985 |
| copyAppendMapStringString |          26.9 |  41.6 |   26.5 |   27.0 |    28.0 |

Copy/append performance measures 1000 iterations of appending a single element
to a list or map field via the `copy {}` DSL. The codec is irrelevant for these
benchmarks since the work is entirely in collection copying.

protobuf-java is fastest on small/medium lists and large maps (its mutable
builder avoids the structural copy). On large lists, protokt leads. On
medium/small maps, protokt codecs are 15-46% faster than both protobuf-java
and Wire.

## Persistent collections

Persistent collections (`PersistentCollectionFactory`) use
`kotlinx-collections-immutable` to back `repeated` and `map` fields with
tree-based persistent data structures. This enables O(log n) structural sharing
on `copy {}` append operations instead of O(n) full copies.

### Impact on core operations

| Benchmark                   | Codec   | Default | Persistent |  Delta |
|-----------------------------|---------|--------:|-----------:|-------:|
| deserializeLargeFromMemory  | PBJ     |     827 |       1219 | +47.4% |
| deserializeLargeFromMemory  | KxIo    |     898 |       1302 | +45.0% |
| deserializeLargeFromMemory  | Protokt |     816 |       1221 | +49.6% |
| deserializeMediumFromMemory | PBJ     |   2.067 |      2.599 | +25.8% |
| deserializeMediumFromMemory | KxIo    |   2.371 |      3.000 | +26.6% |
| deserializeMediumFromMemory | Protokt |   1.895 |      2.648 | +39.8% |
| serializeLargeToMemory      | PBJ     |     141 |        276 | +95.4% |
| serializeLargeToMemory      | KxIo    |     233 |        385 | +65.5% |
| serializeLargeToMemory      | Protokt |     144 |        275 | +90.9% |
| serializeMediumToMemory     | PBJ     |  0.7597 |     0.8798 | +15.8% |
| serializeMediumToMemory     | KxIo    |   1.251 |      1.443 | +15.4% |
| serializeMediumToMemory     | Protokt |  0.7345 |     0.9366 | +27.5% |
| passThroughLargeFromMemory  | PBJ     |     995 |       1654 | +66.3% |
| passThroughLargeFromMemory  | KxIo    |    1145 |       1860 | +62.5% |
| passThroughLargeFromMemory  | Protokt |     990 |       1711 | +72.7% |
| passThroughMediumFromMemory | PBJ     |   3.328 |      4.128 | +24.0% |
| passThroughMediumFromMemory | KxIo    |   4.231 |      5.131 | +21.3% |
| passThroughMediumFromMemory | Protokt |   2.873 |      3.899 | +35.7% |

Persistent collections add 26-50% overhead to large-message deserialization,
66-95% to large-message serialization, and 15-28% to medium-message
serialization.

### Impact on copy/append

| Benchmark                 | Codec   | Default | Persistent |      Delta |
|---------------------------|---------|--------:|-----------:|-----------:|
| copyAppendListLarge       | PBJ     |   1.666 |     0.0376 | **-97.7%** |
| copyAppendListLarge       | KxIo    |   1.688 |     0.0375 | **-97.8%** |
| copyAppendListLarge       | Protokt |   1.710 |     0.0352 | **-97.9%** |
| copyAppendListMedium      | PBJ     |   1.437 |     0.0458 | **-96.8%** |
| copyAppendListMedium      | KxIo    |   1.448 |     0.0418 | **-97.1%** |
| copyAppendListMedium      | Protokt |   1.403 |     0.0424 | **-97.0%** |
| copyAppendListSmall       | PBJ     |   1.466 |      1.498 |      +2.2% |
| copyAppendListSmall       | KxIo    |   1.498 |      1.444 |      -3.6% |
| copyAppendListSmall       | Protokt |   1.565 |      1.463 |      -6.5% |
| copyAppendMapLarge        | PBJ     |    23.7 |     0.2605 | **-98.9%** |
| copyAppendMapLarge        | KxIo    |    24.0 |     0.2634 | **-98.9%** |
| copyAppendMapLarge        | Protokt |    24.0 |     0.2640 | **-98.9%** |
| copyAppendMapMedium       | PBJ     |    13.4 |     0.2489 | **-98.1%** |
| copyAppendMapMedium       | KxIo    |    13.2 |     0.2452 | **-98.1%** |
| copyAppendMapMedium       | Protokt |    13.3 |     0.2446 | **-98.2%** |
| copyAppendMapSmall        | PBJ     |    12.0 |       12.0 |      +0.5% |
| copyAppendMapSmall        | KxIo    |    12.1 |       11.7 |      -3.3% |
| copyAppendMapSmall        | Protokt |    11.7 |       12.0 |      +2.6% |
| copyAppendRepeatedString  | PBJ     |  0.3043 |     0.0213 | **-93.0%** |
| copyAppendRepeatedString  | KxIo    |  0.2949 |     0.0214 | **-92.8%** |
| copyAppendRepeatedString  | Protokt |  0.2985 |     0.0220 | **-92.6%** |
| copyAppendMapStringString | PBJ     |    26.5 |     0.2527 | **-99.0%** |
| copyAppendMapStringString | KxIo    |    27.0 |     0.2588 | **-99.0%** |
| copyAppendMapStringString | Protokt |    28.0 |     0.2611 | **-99.1%** |

Persistent collections provide 30-99x speedup on large/medium-collection append
operations. On small/empty collections, persistent and default perform
identically.

## Kotlin/Native (macosArm64)

Native benchmarks use `ProtoktCodec` (the only codec available on native).
Collection factory is configurable via `collectionFactoryOverride`.

### JVM vs Native (ProtoktCodec, DefaultCollectionFactory)

| Benchmark                         |    JVM | Native | Ratio |
|-----------------------------------|-------:|-------:|------:|
| deserializeLargeFromMemory        |    816 |   1394 |  1.7x |
| deserializeMediumFromMemory       |  1.895 |  5.167 |  2.7x |
| deserializeSmallFromMemory        | 0.0034 | 0.0218 |  6.3x |
| serializeLargeToMemory            |    144 |    366 |  2.5x |
| serializeMediumToMemory           | 0.7345 |  2.449 |  3.3x |
| serializeLargeStreaming           |    197 |    490 |  2.5x |
| passThroughLargeFromMemory        |    990 |   1924 |  1.9x |
| passThroughMediumFromMemory       |  2.873 |  8.706 |  3.0x |
| passThroughStringHeavy            |   18.7 |   18.6 |  1.0x |
| passThroughStringMap              |  7.648 |   10.6 |  1.4x |
| copyAppendListLarge               |  1.710 |  9.927 |  5.8x |
| copyAppendMapLarge                |   24.0 |   70.2 |  2.9x |
| copyAppendMapStringString         |   28.0 |   94.6 |  3.4x |
| mutateAndSerializeStringHeavy     |   52.1 |   94.9 |  1.8x |
| mutateAndSerializeStringVeryHeavy |    507 |    940 |  1.9x |

JVM is 1.7-3.3x faster than native on core serialization/deserialization.
String-heavy pass-through is identical (1.0x) since the work is dominated by
string allocation, not protobuf encoding. Copy/append with default collections
is 2.9-5.8x slower on native.

### Native: Default vs Persistent

| Benchmark                   | Default | Persistent |    Delta |
|-----------------------------|--------:|-----------:|---------:|
| copyAppendListLarge         |   9.927 |     0.4172 | **-96%** |
| copyAppendListMedium        |   8.434 |     0.4229 | **-95%** |
| copyAppendListSmall         |   8.378 |      8.899 |      +6% |
| copyAppendMapLarge          |    70.2 |      1.408 | **-98%** |
| copyAppendMapMedium         |    49.7 |      1.378 | **-97%** |
| copyAppendMapSmall          |    48.9 |       52.1 |      +7% |
| copyAppendRepeatedString    |   7.384 |     0.3242 | **-96%** |
| copyAppendMapStringString   |    94.6 |      1.302 | **-99%** |
| deserializeLargeFromMemory  |    1394 |       1817 |     +30% |
| deserializeMediumFromMemory |   5.167 |      7.362 |     +42% |
| serializeLargeToMemory      |     366 |        528 |     +44% |
| serializeMediumToMemory     |   2.449 |      2.206 |     -10% |
| passThroughLargeFromMemory  |    1924 |       2599 |     +35% |
| passThroughMediumFromMemory |   8.706 |       10.8 |     +24% |

Persistent collections on native show the same pattern as JVM: 95-99% speedup
on large/medium copy-append operations, with 30-44% overhead on
deserialization/serialization. The copy-append speedup is especially impactful
on native, where default-collection copy/append is 3-6x slower than JVM.

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
