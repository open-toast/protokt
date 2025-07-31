/*
 * Copyright (c) 2025 Toast, Inc.
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

package build.buf.protovalidate

import com.google.protobuf.Descriptors.Descriptor
import dev.cel.bundle.CelFactory
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.toDynamicMessage

internal class ProtoktEvaluatorBuilder(
    private val evaluatorBuilder: EvaluatorBuilder
) {
    constructor(config: Config) : this(
        EvaluatorBuilder(
            ValidateLibrary().let {
                CelFactory.standardCelBuilder()
                    .addCompilerLibraries(it)
                    .addRuntimeLibraries(it)
                    .build()
            },
            config
        )
    )

    fun load(descriptor: Descriptor) =
        ProtoktEvaluator(evaluatorBuilder.load(descriptor))
}

internal class ProtoktEvaluator(
    private val evaluator: Evaluator
) {
    fun evaluate(message: Message, runtimeContext: RuntimeContext, failFast: Boolean) =
        evaluator.evaluate(MessageValue(message.toDynamicMessage(runtimeContext)), failFast)
            .let(::ProtoktRuleViolationBuilders)
}

internal class ProtoktRuleViolationBuilders(
    private val ruleViolationBuilders: List<RuleViolation.Builder>
) {
    fun isEmpty() =
        ruleViolationBuilders.isEmpty()

    fun build() =
        ValidationResult(ruleViolationBuilders.map { it.build() })
}
