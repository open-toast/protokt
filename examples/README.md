# Protokt gRPC examples

## Examples

This directory contains several Protokt gRPC examples.

## File organization

The example sources are organized into the following top-level folders:

- [protos](protos): `.proto` files (shared across examples)
- [grpc-java](grpc-java): implementation of client and server using grpc-java bindings
- [grpc-java-lite](grpc-java-lite): implementation of client using grpc-java bindings and protokt-lite (TODO)
- [grpc-kotlin](grpc-kotlin): implementation of client and server using grpc-kotlin bindings
- [grpc-kotlin-lite](grpc-kotlin-lite): implementation of client using grpc-kotlin bindings and protokt-lite (TODO)

## Instructions for running examples

### Animals

Start a server, either grpc-java- or grpc-kotlin-based:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:AnimalsServer
```

In another console, run either client against the "dog", "pig", and "sheep" services:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:AnimalsClient --args=dog
protokt % ./gradlew :examples:grpc-[java|kotlin]:AnimalsClient --args=pig
protokt % ./gradlew :examples:grpc-[java|kotlin]:AnimalsClient --args=sheep
```

### Greeter

Start a server, either grpc-java- or grpc-kotlin-based:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:HelloWorldServer
```

In another console, run either client:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:HelloWorldClient
```

### Route Guide

Start a server, either grpc-java- or grpc-kotlin-based:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:RouteGuideServer
```

In another console, run either client:

```sh
protokt % ./gradlew :examples:grpc-[java|kotlin]:RouteGuideClient
```
