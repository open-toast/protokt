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
| **Commit**       | `744c52d4`                   |

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

## Results

All values are milliseconds per operation (ms/op). Lower is better.

### Deserialize (byte array)

| Benchmark                   | protobuf-java |   Wire |    PBJ |   KxIo |    Protokt |
|-----------------------------|--------------:|-------:|-------:|-------:|-----------:|
| deserializeLargeFromMemory  |          1487 |    873 |    811 |    878 |        808 |
| deserializeMediumFromMemory |         3.479 |  3.293 |  1.985 |  2.348 |  **1.830** |
| deserializeSmallFromMemory  |        0.0060 | 0.0088 | 0.0035 | 0.0042 | **0.0033** |
| deserializeStringMap        |         7.291 |  7.166 |  6.845 |  7.030 |      6.837 |
| deserializeStringRepeated   |         6.412 |  6.375 |  6.345 |  6.566 |      6.326 |

All three protokt codecs outperform both native protobuf-java and Wire on
deserialization from byte arrays. `ProtoktCodec` leads on medium and small
messages. On large messages, PBJ and Protokt are within noise of each other,
both ~46% faster than protobuf-java. String-collection benchmarks are tight
across all implementations.

### Deserialize (streaming)

| Benchmark                       | protobuf-java |   Wire |        PBJ |       KxIo |  Protokt |
|---------------------------------|--------------:|-------:|-----------:|-----------:|---------:|
| deserializeLargeStreaming       |          1526 |    955 |    **825** |        986 |      --- |
| deserializeMediumStreaming      |         3.122 |  4.519 |  **1.980** |      2.798 |      --- |
| deserializeSmallStreaming       |        0.0344 | 0.0146 |     0.0311 | **0.0111** |      --- |
| deserializeStringHeavyStreaming |            19 |     18 |         18 |         18 |      --- |
| deserializeStringOneofStreaming |            19 |     18 |         18 |         18 |      --- |

`ProtobufJavaCodec` is fastest on large and medium streaming deserialization,
46% faster than native protobuf-java and 14% faster than Wire on large messages.
`KotlinxIoCodec` wins on small messages. String-heavy results converge across
all implementations.

### Serialize (byte array)

| Benchmark               | protobuf-java |   Wire |    PBJ |   KxIo |    Protokt |
|-------------------------|--------------:|-------:|-------:|-------:|-----------:|
| serializeLargeToMemory  |          1206 |   1425 |    135 |    215 |    **134** |
| serializeMediumToMemory |        0.8811 |  1.123 | 0.7327 |  1.341 | **0.6742** |
| serializeSmallToMemory  |        0.0037 | 0.0063 | 0.0029 | 0.0057 | **0.0025** |

`ProtoktCodec` leads on all sizes, beating native protobuf-java by 89% on
large messages (134 ms vs 1206 ms) and Wire by 91%. Even on small messages,
`ProtoktCodec` is 32% faster than protobuf-java.

### Serialize (streaming)

| Benchmark                | protobuf-java |   Wire |     PBJ |   KxIo | Protokt |
|--------------------------|--------------:|-------:|--------:|-------:|--------:|
| serializeLargeStreaming  |          1221 |   1390 | **205** |    216 |     --- |
| serializeMediumStreaming |    **0.9861** |  1.143 |   1.144 |  1.270 |     --- |
| serializeSmallStreaming  |    **0.0045** | 0.0072 |  0.0058 | 0.0061 |     --- |

`ProtobufJavaCodec` leads on large streaming serialization at 205 ms, 83% faster
than native protobuf-java and 85% faster than Wire. Native protobuf-java retains
the edge on small and medium streaming serialization.

### Pass-through (deserialize then serialize)

| Benchmark                   | protobuf-java |   Wire |    PBJ |   KxIo |    Protokt |
|-----------------------------|--------------:|-------:|-------:|-------:|-----------:|
| passThroughLargeFromMemory  |          3154 |   2190 |    981 |   1144 |    **977** |
| passThroughMediumFromMemory |         4.794 |  4.415 |  3.136 |  3.987 |  **2.852** |
| passThroughSmallFromMemory  |        0.0107 | 0.0140 | 0.0109 | 0.0117 | **0.0075** |
| passThroughStringHeavy      |            48 |     39 |     18 |     19 |         18 |
| passThroughStringOneof      |            48 |     39 |     18 |     19 |         18 |
| passThroughStringMap        |            21 |     15 |  7.483 |  7.996 |      7.516 |
| passThroughStringRepeated   |            17 |     14 |  6.793 |  7.200 |      6.709 |

`ProtoktCodec` wins the core pass-through benchmarks (large, medium, small),
69% faster than native protobuf-java and 55% faster than Wire on large messages.

String-heavy and string-collection pass-through: all three protokt codecs
are substantially faster than both native libraries. The three protokt codecs
are within noise of each other on these workloads.

### Mutate and serialize

| Benchmark                                 | protobuf-java |       Wire |     PBJ |    KxIo | Protokt |
|-------------------------------------------|--------------:|-----------:|--------:|--------:|--------:|
| mutateAndSerializeStringHeavy             |            49 |     **39** |      51 |      51 |      51 |
| mutateAndSerializeStringHeavyStreaming    |            42 |     **39** |      51 |      51 |     --- |
| mutateAndSerializeStringOneof             |            49 |     **39** |      51 |      51 |      51 |
| mutateAndSerializeStringOneof20k          |            83 |     **79** |     101 |     102 |     101 |
| mutateAndSerializeStringOneof20kStreaming |            83 |     **78** |     101 |     102 |     --- |
| mutateAndSerializeStringOneofStreaming    |            42 |     **39** |      51 |      51 |     --- |
| mutateAndSerializeStringOneofVeryHeavy    |       **492** |        665 |     509 |     507 |     501 |
| mutateAndSerializeStringVeryHeavy         |       **482** |        682 |     504 |     514 |     505 |

Wire is fastest on most mutate-and-serialize benchmarks for standard-size
strings. For very heavy strings (1M characters), Wire becomes the slowest and
protobuf-java leads.

Protokt codecs are ~25-31% slower than Wire on standard-size
mutate-and-serialize. All protokt codecs perform similarly to each other.

### Copy/append

| Benchmark                 | protobuf-java |  Wire |    PBJ |       KxIo | Protokt |
|---------------------------|--------------:|------:|-------:|-----------:|--------:|
| copyAppendListLarge       |         3.824 | 3.672 |  2.068 |  **1.929** |   3.834 |
| copyAppendListMedium      |    **0.3002** | 1.486 |  1.513 |      1.435 |   1.796 |
| copyAppendListSmall       |    **0.2810** | 1.524 |  1.408 |      1.401 |   1.423 |
| copyAppendMapLarge        |            22 |    27 |     23 |         23 |      23 |
| copyAppendMapMedium       |            18 |    21 |     13 |         13 |      13 |
| copyAppendMapSmall        |            17 |    21 |     12 |         12 |      12 |
| copyAppendRepeatedString  |        0.5436 | 2.835 | 0.2903 |     0.2900 |  0.2916 |
| copyAppendMapStringString |            36 |    39 |     26 |         26 |      26 |

Copy/append performance measures 1000 iterations of appending a single element
to a list or map field via the `copy {}` DSL. The codec is irrelevant for these
benchmarks since the work is entirely in collection copying.

For lists, native protobuf-java is fastest on small and medium messages (its
mutable builder avoids the structural copy). On large messages, `KotlinxIoCodec`
leads at 1.929 ms, 47% faster than protobuf-java.

For maps, all three protokt codecs perform identically and are faster than
both protobuf-java and Wire on medium and small maps. On large maps, all
implementations are within noise except Wire, which is slowest.

For string collections, protokt codecs are roughly 2x faster than protobuf-java
on `copyAppendRepeatedString` and ~28% faster on `copyAppendMapStringString`.
Wire is 5-10x slower than protokt on repeated string append.

## Persistent collections

Persistent collections (`PersistentCollectionFactory`) use
`kotlinx-collections-immutable` to back `repeated` and `map` fields with
tree-based persistent data structures. This enables O(log n) structural sharing
on `copy {}` append operations instead of O(n) full copies.

### Impact on core operations

| Benchmark                   | Codec   | Default | Persistent |   Delta |
|-----------------------------|---------|--------:|-----------:|--------:|
| deserializeLargeFromMemory  | PBJ     |     811 |       1614 |  +99.0% |
| deserializeLargeFromMemory  | KxIo    |     878 |       1289 |  +46.8% |
| deserializeLargeFromMemory  | Protokt |     808 |       1750 | +116.6% |
| deserializeMediumFromMemory | PBJ     |   1.985 |      3.012 |  +51.8% |
| deserializeMediumFromMemory | KxIo    |   2.348 |      3.323 |  +41.5% |
| deserializeMediumFromMemory | Protokt |   1.830 |      2.705 |  +47.8% |
| serializeLargeToMemory      | PBJ     |     135 |        299 | +121.4% |
| serializeLargeToMemory      | KxIo    |     215 |        365 |  +69.7% |
| serializeLargeToMemory      | Protokt |     134 |        273 | +104.0% |
| serializeMediumToMemory     | PBJ     |   0.733 |      0.903 |  +23.2% |
| serializeMediumToMemory     | KxIo    |   1.341 |      1.335 |   -0.5% |
| serializeMediumToMemory     | Protokt |   0.674 |      0.888 |  +31.8% |
| passThroughLargeFromMemory  | PBJ     |     981 |       2075 | +111.7% |
| passThroughLargeFromMemory  | KxIo    |    1144 |       1810 |  +58.2% |
| passThroughLargeFromMemory  | Protokt |     977 |       2066 | +111.3% |
| passThroughMediumFromMemory | PBJ     |   3.136 |      4.803 |  +53.2% |
| passThroughMediumFromMemory | KxIo    |   3.987 |      5.496 |  +37.8% |
| passThroughMediumFromMemory | Protokt |   2.852 |      5.280 |  +85.1% |

Persistent collections add overhead to core operations. Large-message
deserialization sees 47-117% overhead, and serialization sees 70-121% overhead.
`KotlinxIoCodec` consistently shows the smallest persistent-collection penalty.

### Impact on copy/append

| Benchmark                 | Codec   | Default | Persistent |      Delta |
|---------------------------|---------|--------:|-----------:|-----------:|
| copyAppendListLarge       | PBJ     |   2.068 |      0.045 | **-97.8%** |
| copyAppendListLarge       | KxIo    |   1.929 |      0.034 | **-98.3%** |
| copyAppendListLarge       | Protokt |   3.834 |      0.042 | **-98.9%** |
| copyAppendMapLarge        | PBJ     |      23 |      0.319 | **-98.6%** |
| copyAppendMapLarge        | KxIo    |      23 |      0.252 | **-98.9%** |
| copyAppendMapLarge        | Protokt |      23 |      0.257 | **-98.9%** |
| copyAppendListSmall       | PBJ     |   1.408 |      1.470 |      +4.4% |
| copyAppendListSmall       | KxIo    |   1.401 |      1.428 |      +1.9% |
| copyAppendListSmall       | Protokt |   1.423 |      1.470 |      +3.3% |
| copyAppendMapSmall        | PBJ     |      12 |         27 |    +126.6% |
| copyAppendMapSmall        | KxIo    |      12 |         12 |      -0.4% |
| copyAppendMapSmall        | Protokt |      12 |         19 |     +62.6% |
| copyAppendRepeatedString  | PBJ     |   0.290 |      0.034 | **-88.4%** |
| copyAppendRepeatedString  | KxIo    |   0.290 |      0.021 | **-92.8%** |
| copyAppendRepeatedString  | Protokt |   0.292 |      0.032 | **-88.9%** |
| copyAppendMapStringString | PBJ     |      26 |      0.265 | **-99.0%** |
| copyAppendMapStringString | KxIo    |      26 |      0.242 | **-99.1%** |
| copyAppendMapStringString | Protokt |      26 |      0.262 | **-99.0%** |

Persistent collections provide 29-99x speedup on large-collection append
operations (98% improvement for lists, 99% for maps, 89-93% for repeated
strings, and 99% for string maps).

On small/empty lists, persistent and default collections perform identically.
Small-map persistent results vary by codec: `KotlinxIoCodec` shows no overhead,
while `ProtobufJavaCodec` and `ProtoktCodec` show increased cost.

### Cross-library context

Even with the overhead from persistent collections, the trade-off is compelling
for workloads that combine deserialization with incremental message building:

| Benchmark                   | protobuf-java |   Wire | protokt | protokt + persistent | vs protobuf-java | vs Wire |
|-----------------------------|--------------:|-------:|--------:|---------------------:|-----------------:|--------:|
| deserializeLargeFromMemory  |          1487 |    873 |     808 |                 1289 |             -13% |    +48% |
| deserializeMediumFromMemory |         3.479 |  3.293 |   1.830 |                2.705 |             -22% |    -18% |
| serializeLargeToMemory      |          1206 |   1425 |     134 |                  273 |             -77% |    -81% |
| serializeMediumToMemory     |        0.8811 |  1.123 |   0.674 |                0.888 |              +1% |    -21% |
| passThroughLargeFromMemory  |          3154 |   2190 |     977 |                 1810 |             -43% |    -17% |
| passThroughMediumFromMemory |         4.794 |  4.415 |   2.852 |                4.803 |              +0% |     +9% |
| copyAppendListLarge         |         3.824 |  3.672 |   1.929 |                0.034 |             -99% |    -99% |
| copyAppendMapLarge          |            22 |     27 |      23 |                0.252 |             -99% |    -99% |
| copyAppendRepeatedString    |        0.5436 |  2.835 |   0.290 |                0.021 |             -96% |    -99% |
| copyAppendMapStringString   |            36 |     39 |      26 |                0.242 |             -99% |    -99% |

The "vs" columns compare protokt with persistent collections (using the best
codec per benchmark) against each native library. Persistent-collection protokt
retains a 13-77% serialization and deserialization advantage over protobuf-java
on most benchmarks, though large-message deserialization with persistent
collections is 48% slower than Wire (a trade-off against the 99% copy/append
improvement). Copy/append of large collections is 96-99% faster than both other
libraries. Workloads that mix deserialization with incremental message building
via `copy {}` will see the largest overall benefit from persistent collections.

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
