package protokt.v1.buf.validate

import build.buf.protovalidate.internal.evaluator.MessageLike
import build.buf.protovalidate.internal.evaluator.Value
import com.google.protobuf.Descriptors.FieldDescriptor
import protokt.v1.Bytes
import protokt.v1.Enum
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.getField
import protokt.v1.google.protobuf.hasField

internal class ProtoktMessageLike(
    val message: Message,
    val context: RuntimeContext,
) : MessageLike {
    override fun hasField(field: FieldDescriptor) =
        message.hasField(field)

    override fun getField(field: FieldDescriptor) =
        ProtoktObjectValue(
            field,
            message.getField(field)!!,
            context
        )
}

internal class ProtoktMessageValue(
    private val message: Message,
    private val context: RuntimeContext,
) : Value {
    override fun messageValue() =
        ProtoktMessageLike(message, context)

    override fun repeatedValue() =
        emptyList<Value>()

    override fun mapValue() =
        emptyMap<Value, Value>()

    override fun celValue() =
        context.convertValue(message)

    override fun <T : Any> jvmValue(clazz: Class<T>) =
        null
}

internal class ProtoktObjectValue(
    private val fieldDescriptor: FieldDescriptor,
    private val value: Any,
    private val context: RuntimeContext,
) : Value {
    override fun messageValue(): MessageLike =
        ProtoktMessageLike(value as Message, context)

    override fun repeatedValue() =
        (value as List<*>).map { ProtoktObjectValue(fieldDescriptor, it!!, context) }

    override fun mapValue(): Map<Value, Value> {
        val input = value as Map<*, *>

        val keyDesc = fieldDescriptor.messageType.findFieldByNumber(1)
        val valDesc = fieldDescriptor.messageType.findFieldByNumber(2)

        return input.entries.associate { (key, value) ->
            Pair(
                ProtoktObjectValue(keyDesc, key!!, context),
                ProtoktObjectValue(valDesc, value!!, context),
            )
        }
    }

    override fun celValue() =
        when (value) {
            is Enum -> value.value
            is UInt -> org.projectnessie.cel.common.ULong.valueOf(value.toLong())
            is ULong -> org.projectnessie.cel.common.ULong.valueOf(value.toLong())
            is Message, is Bytes -> context.convertValue(value)

            // pray
            else -> value
        }

    override fun <T : Any> jvmValue(clazz: Class<T>): T? =
        context.convertValue(value)?.let(clazz::cast)
}
