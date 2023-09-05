test -f ci/protobuf/conformance_test_runner || { cd ci/protobuf && git submodule update --init --recursive && cmake . -DCMAKE_CXX_STANDARD=14 -Dprotobuf_BUILD_CONFORMANCE=ON && cmake --build . }
