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

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val extensionRegistry = ExtensionRegistry.newInstance()
        extensionRegistry.add(ValidateProto.message)
        extensionRegistry.add(ValidateProto.field)
        extensionRegistry.add(ValidateProto.oneof)
        val request = TestConformanceRequest.parseFrom(System.`in`, extensionRegistry)
        val response = testConformance(request)
        response.writeTo(System.out)
    }

    private fun testConformance(request: TestConformanceRequest): TestConformanceResponse {
        val descriptorMap = parse(request.fdset)
        val validator = ProtoktValidator()
        val responseBuilder = TestConformanceResponse.newBuilder()
        responseBuilder.putAllResults(
            request.casesMap.mapValues { (_, value) ->
                testCase(validator, descriptorMap, value)
            }
        )
        return responseBuilder.build()
    }

    private fun testCase(
        validator: ProtoktValidator,
        fileDescriptors: Map<String, Descriptors.Descriptor>,
        testCase: com.google.protobuf.Any
    ): TestResult {
        val urlParts = testCase.typeUrl.split('/', limit = 2)
        val fullName = urlParts[urlParts.size - 1]
        fileDescriptors[fullName] ?: return unexpectedErrorResult("Unable to find descriptor: %s", fullName)
        val testCaseValue = testCase.value
        val message = DynamicConcreteMessageDeserializer.parse(fullName, testCaseValue.newInput())
        return validate(validator, message, fileDescriptors.values)
    }

    private fun validate(
        validator: ProtoktValidator,
        message: Message,
        descriptors: Iterable<Descriptors.Descriptor>
    ): TestResult {
        try {
            descriptors.forEach { validator.load(it, message) }
            val result = validator.validate(message)
            if (result.isSuccess) {
                return TestResult.newBuilder().setSuccess(true).build()
            }
            val error = Violations.newBuilder().addAllViolations(result.violations).build()
            return TestResult.newBuilder().setValidationError(error).build()
        } catch (e: CompilationException) {
            return TestResult.newBuilder().setCompilationError(e.message).build()
        } catch (e: ExecutionException) {
            return TestResult.newBuilder().setRuntimeError(e.message).build()
        } catch (e: Exception) {
            return unexpectedErrorResult("unknown error: %s", e.toString())
        }
    }

    private fun unexpectedErrorResult(format: String?, vararg args: Any?): TestResult {
        val errorMessage = String.format(format!!, *args)
        return TestResult.newBuilder().setUnexpectedError(errorMessage).build()
    }
}
