# Conformance Tests

In order to run the Kotlin/JS conformance tests, you must have Node, ProtobufJS, and Long.js installed:

```bash
protokt % npm install protobufjs@6.11.3
protokt % npm install long@5.2.0
```

## Building the Conformance Runner

(assumed running on a Mac)

```sh
git clone git@github.com:protocolbuffers/protobuf
cd protobuf/
```

Mac conformance tests:

```sh
protobuf % cmake . -DCMAKE_CXX_STANDARD=14 -Dprotobuf_BUILD_CONFORMANCE=ON && cmake --build .
protobuf % cp bin/conformance-test-runner ../protokt/testing/conformance/runner/bin/darwin/conformance-test-runner
```

Ubuntu conformance tests:

```sh
protobuf % git clean -fdx
protobuf % docker run -v $(pwd):/tmp -t -i ubuntu:20.04 /bin/bash
root@38f7a53696b9:/# apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y build-essential cmake
root@38f7a53696b9:/# cd tmp/
root@38f7a53696b9:/tmp# cmake . -DCMAKE_CXX_STANDARD=14 -Dprotobuf_BUILD_CONFORMANCE=ON && cmake --build .
root@38f7a53696b9:/tmp/conformance# exit
protobuf % cp conformance_test_runner ../protokt/testing/conformance/runner/bin/ubuntu-16.04-x86_64/conformance-test-runner
```
