# Benchmarks

## Running

Run all benchmarks for a given implementation:

```
./gradlew :benchmarks:protokt-benchmarks:run
./gradlew :benchmarks:protobuf-java-benchmarks:run
./gradlew :benchmarks:wire-benchmarks:run
```

## Flags

Flags are passed via `--args`:

| Flag | Description |
|------|-------------|
| `-i regex` | Include only benchmarks matching regex |
| `-e regex` | Exclude benchmarks matching regex |
| `-p name=value` | Set a JMH parameter |

When no `-i` flag is given, all benchmarks in the class are included.

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

Combine flags:

```
./gradlew :benchmarks:protokt-benchmarks:run --args="-i serialize -e .*String.*"
```
