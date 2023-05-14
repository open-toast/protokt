/*
 * Copyright (c) 2023 Toast, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.protokt.v1.conformance

import com.toasttab.protokt.v1_test_messages.proto3.TestAllTypesProto3
import org.junit.jupiter.api.Test

/**
 * Use to parse the error message output from protobuf conformance tests, e.g.:
 *
 *     ERROR, test=Required.Proto3.ProtobufInput.ValidDataScalar.INT64[2].ProtobufOutput: Failed to parse input or produce output. request=protobuf_payload: "\020\377\377\377\377\377\377\377\377\177" requested_output_format: PROTOBUF message_type: "protobuf_test_messages.proto3.TestAllTypesProto3" test_category: BINARY_TEST, response=serialize_error: "IllegalStateException: Expected 10, got 2\n    at AbstractKtMessage.ce (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/protokt-protokt-runtime.js:967:13)\n    at Platform.w11 (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/protokt-driver.js:12091:33)\n    at main$slambda.la (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/protokt-driver.js:11739:61)\n    at CoroutineImpl.ka (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/kotlin-kotlin-stdlib-js-ir.js:5232:33)\n    at CoroutineImpl.f2 (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/kotlin-kotlin-stdlib-js-ir.js:5278:17)\n    at DispatchedTask.mm (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/kotlinx.coroutines-kotlinx-coroutines-core-js-ir.js:2428:24)\n    at MessageQueue.zq (/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/kotlinx.coroutines-kotlinx-coroutines-core-js-ir.js:2877:19)\n    at /Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js-ir/build/compileSync/js/main/productionExecutable/kotlin/kotlinx.coroutines-kotlinx-coroutines-core-js-ir.js:2823:14\n    at process.processTicksAndRejections (node:internal/process/task_queues:77:11)\n"
 *
 */
class ParseConformanceOctal {
    @Test
    fun `parse octal into conformance body`() {
        val octal = """
            \020\200\200\200\200\200\200\200\200\200\001
        """.trim()

        val parsed =
            octal
                .removePrefix("\\")
                .split("\\")
                .map { Integer.parseInt(it, 8) }
                .map { it.toByte() }
                .toByteArray()

        val request = TestAllTypesProto3.deserialize(parsed)

        println(request)
    }
}
