if [ ! -f "ci/protobuf/conformance_test_runner" ]; then
    cd ci/protobuf
    git submodule update --init --recursive
    cmake . -DCMAKE_CXX_STANDARD=14 -Dprotobuf_BUILD_CONFORMANCE=ON
    cmake --build .
fi
