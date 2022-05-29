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
protobuf % ./configure
protobuf % make clean
protobuf % make
protobuf % cd conformance
conformance % make
conformance % cd ..
protobuf % cp conformance/.libs/conformance-test-runner ../protokt/testing/conformance-driver/bin/darwin/conformance-test-runner
protobuf % rm ../protokt/testing/conformance-driver/bin/darwin/.libs/*
protobuf % cp src/.libs/libprotobuf.xx.dylib ../protokt/testing/conformance-driver/bin/darwin/.libs/
```

Ubuntu conformance tests:

```sh
protobuf % docker run -v $(pwd):/tmp -t -i ubuntu:16.04 /bin/bash
root@38f7a53696b9:/# apt-get update && apt-get install build-essential
root@38f7a53696b9:/# cd tmp/
root@38f7a53696b9:/tmp# ./configure
root@38f7a53696b9:/tmp# make clean
root@38f7a53696b9:/tmp# make
root@38f7a53696b9:/tmp# cd conformance
root@38f7a53696b9:/tmp/conformance# make
root@38f7a53696b9:/tmp/conformance# exit
protobuf % cp conformance/.libs/conformance-test-runner ../protokt/testing/conformance-driver/bin/ubuntu-16.04-x86_64/conformance-test-runner
protobuf % rm ../protokt/testing/conformance-driver/bin/ubuntu-16.04-x86_64/.libs/*
protobuf % cp src/.libs/libprotobuf.so.xx ../protokt/testing/conformance-driver/bin/ubuntu-16.04-x86_64/.libs/
```

Note that the `xx` version numbers on `libprotobuf.so.xx` and `libprotobuf.xx.dylib` will change.
