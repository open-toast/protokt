Steps taken to build conformance tests (assumed running on a Mac):

```
git clone git@github.com:protocolbuffers/protobuf
cd protobuf/
```

Mac conformance tests:

```
protobuf % ./configure
protobuf % make clean
protobuf % make
protobuf % cd conformance/
conformance % make
conformance % cp .libs/conformance-test-runner ../../protokt/testing/conformance-driver/bin/darwin/conformance-test-runner
```

Ubuntu conformance tests:
```
protobuf % docker run -v $(pwd):/tmp -t -i ubuntu:16.04 /bin/bash
root@38f7a53696b9:/# apt-get update && apt-get install build-essential
root@38f7a53696b9:/# cd tmp/
root@38f7a53696b9:/tmp# ./configure
root@38f7a53696b9:/tmp# make clean
root@38f7a53696b9:/tmp# make
root@38f7a53696b9:/tmp# cd conformance/
root@38f7a53696b9:/tmp/conformance# make
root@38f7a53696b9:/tmp/conformance# exit
protobuf % cp conformance/.libs/conformance-test-runner ../protokt/testing/conformance-driver/bin/ubuntu-16.04-x86_64/conformance-test-runner
protobuf % cp src/.libs/libprotobuf.so.23 ../protokt/testing/conformance-driver/bin/ubuntu-16.04-x86_64/.libs/libprotobuf.so.23
```

Note that the integer suffix on `libprotobuf.so` may change, and the need to
copy it may disappear as it has for running the conformance tests on Mac.
