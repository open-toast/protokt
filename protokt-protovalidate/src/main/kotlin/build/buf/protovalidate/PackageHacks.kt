package build.buf.protovalidate

import com.google.protobuf.Descriptors.Descriptor
import org.projectnessie.cel.Env
import org.projectnessie.cel.Library
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.toDynamicMessage

internal class ProtoktEvaluatorBuilder(
    private val evaluatorBuilder: EvaluatorBuilder
) {
    constructor(config: Config) : this(EvaluatorBuilder(Env.newEnv(Library.Lib(ValidateLibrary())), config))

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
