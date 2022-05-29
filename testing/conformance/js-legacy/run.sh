#!/usr/bin/env bash

set -e
set -o pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

exec node $DIR/build/compileSync/main/productionExecutable/kotlin/protokt-js-legacy.js
