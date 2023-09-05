# Protokt gRPC examples

## Examples

This directory contains several protokt gRPC examples.

## File organization

The example sources are organized into the following top-level folders:

- [protos](protos): `.proto` files (shared across examples)
- [grpc-java](grpc-java): implementation of client and server using grpc-java bindings
- [grpc-java-lite](grpc-java-lite): implementation of client and server using grpc-java bindings and protokt-lite
- [grpc-kotlin](grpc-kotlin): implementation of client and server using grpc-kotlin bindings
- [grpc-kotlin-lite](grpc-kotlin-lite): implementation of client and server using grpc-kotlin bindings and protokt-lite
- [grpc-node](grpc-node): implementation of client and server using bindings inspired by grpc-kotlin running on NodeJS

## Instructions for running examples

### Animals

Start a server based on any of the supported bindings:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:AnimalsServer
```

In another console, run a client against the "dog", "pig", and "sheep" services:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:AnimalsClient --args=dog
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:AnimalsClient --args=pig
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:AnimalsClient --args=sheep
```

### Greeter

Start a server based on any of the supported bindings:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:HelloWorldServer
```

In another console, run any client:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:HelloWorldClient
```

### Route Guide

Start a server based on any of the supported bindings:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:RouteGuideServer
```

In another console, run any client:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin|java-lite|kotlin-lite|node]:RouteGuideClient
```
