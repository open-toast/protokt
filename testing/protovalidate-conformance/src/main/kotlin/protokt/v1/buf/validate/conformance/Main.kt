/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.buf.validate.conformance

import buf.validate.conformance.harness.Harness.TestConformanceRequest
import buf.validate.conformance.harness.Harness.TestConformanceResponse
import buf.validate.conformance.harness.Harness.TestResult
import build.buf.protovalidate.exceptions.CompilationException
import build.buf.protovalidate.exceptions.ExecutionException
import build.buf.validate.ValidateProto
import build.buf.validate.Violations
import com.google.protobuf.Descriptors
import com.google.protobuf.ExtensionRegistry
import protokt.v1.Message
import protokt.v1.buf.validate.ProtoktValidator
import java.io.File
import java.time.Instant

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val extensionRegistry = ExtensionRegistry.newInstance()
        extensionRegistry.add(ValidateProto.message)
        extensionRegistry.add(ValidateProto.field)
        extensionRegistry.add(ValidateProto.oneof)
        val request = TestConformanceRequest.parseFrom(System.`in`, extensionRegistry)
        testConformance(request).writeTo(System.out)
    }

    private fun testConformance(request: TestConformanceRequest): TestConformanceResponse {
        val descriptorMap = parse(request.fdset)
        val validator = ProtoktValidator()
        loadValidDescriptors(validator, descriptorMap.values)
        return TestConformanceResponse
            .newBuilder()
            .putAllResults(
                request.casesMap.mapValues { (_, value) ->
                    testCase(validator, descriptorMap, value)
                }
            )
            .build()
    }

    private fun loadValidDescriptors(validator: ProtoktValidator, descriptors: Iterable<Descriptors.Descriptor>) {
        descriptors.forEach {
            try {
                validator.load(it)
            } catch (_: CompilationException) {
                // leave failures for later; they trigger specific conformance results
            }
        }
    }

    private fun testCase(
        validator: ProtoktValidator,
        fileDescriptors: Map<String, Descriptors.Descriptor>,
        testCase: com.google.protobuf.Any
    ): TestResult {
        val urlParts = testCase.typeUrl.split('/', limit = 2)
        val fullName = urlParts[urlParts.size - 1]
        val descriptor = fileDescriptors[fullName] ?: return unexpected("Unable to find descriptor $fullName")

        try {
            validator.load(descriptor)
        } catch (e: CompilationException) {
            return TestResult.newBuilder().setCompilationError(e.message).build()
        }

        return validate(validator, DynamicConcreteMessageDeserializer.parse(fullName, testCase.value))
    }

    private fun validate(validator: ProtoktValidator, message: Message) =
        try {
            val result = validator.validate(message)
            if (result.isSuccess) {
                TestResult.newBuilder().setSuccess(true).build()
            } else {
                TestResult.newBuilder()
                    .setValidationError(Violations.newBuilder().addAllViolations(result.violations).build())
                    .build()
            }
        } catch (e: ExecutionException) {
            TestResult.newBuilder().setRuntimeError(e.message).build()
        } catch (e: Exception) {
            unexpected("unknown error: $e")
        }

    private fun unexpected(message: String) =
        TestResult.newBuilder().setUnexpectedError(message).build()
}
