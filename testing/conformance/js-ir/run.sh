#!/usr/bin/env bash

set -e
set -o pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

exec node $DIR/build/compileSync/js/main/productionExecutable/kotlin/protokt-testing-conformance-js-ir.js 2> $DIR/build/conformance-run
