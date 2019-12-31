#!/usr/bin/env bash

if [ -z "$TEST_RUNNER_PATH" ]; then
    echo "Set TEST_RUNNER_PATH to conformance-test-runner"
    exit 1
fi

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

$TEST_RUNNER_PATH/conformance-test-runner --enforce_recommended $DIR/build/install/protokt-conformance/bin/protokt-conformance
