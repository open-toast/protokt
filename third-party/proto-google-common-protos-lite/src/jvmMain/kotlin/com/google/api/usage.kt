@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/usage.proto
package com.google.api

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.copyList
import com.toasttab.protokt.rt.finishList
import com.toasttab.protokt.rt.sizeof
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList

/**
 * Configuration controlling usage of a service.
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.Usage")
class Usage private constructor(
    /**
     * Requirements that must be satisfied before a consumer project can use the service. Each
     * requirement is of the form <service.name>/<requirement-id>; for example
     * 'serviceusage.googleapis.com/billing-enabled'.
     *
     *  For Google APIs, a Terms of Service requirement must be included here. Google Cloud APIs
     * must include "serviceusage.googleapis.com/tos/cloud". Other Google APIs should include
     * "serviceusage.googleapis.com/tos/universal". Additional ToS can be included based on the
     * business needs.
     */
    val requirements: List<String>,
    /**
     * A list of usage rules that apply to individual API methods.
     *
     *  **NOTE:** All service configuration rules follow "last one wins" order.
     */
    val rules: List<UsageRule>,
    /**
     * The full resource name of a channel used for sending notifications to the service producer.
     *
     *  Google Service Management currently only supports [Google Cloud
     * Pub/Sub](https://cloud.google.com/pubsub) as a notification channel. To use Google Cloud Pub/Sub
     * as the channel, this must be the name of a Cloud Pub/Sub topic that uses the Cloud Pub/Sub topic
     * name format documented in https://cloud.google.com/pubsub/docs/overview.
     */
    val producerNotificationChannel: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (requirements.isNotEmpty()) {
            result += (sizeof(Tag(1)) * requirements.size) + requirements.sumOf { sizeof(it) }
        }
        if (rules.isNotEmpty()) {
            result += (sizeof(Tag(6)) * rules.size) + rules.sumOf { sizeof(it) }
        }
        if (producerNotificationChannel.isNotEmpty()) {
            result += sizeof(Tag(7)) + sizeof(producerNotificationChannel)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (requirements.isNotEmpty()) {
            requirements.forEach { serializer.write(Tag(10)).write(it) }
        }
        if (rules.isNotEmpty()) {
            rules.forEach { serializer.write(Tag(50)).write(it) }
        }
        if (producerNotificationChannel.isNotEmpty()) {
            serializer.write(Tag(58)).write(producerNotificationChannel)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Usage &&
        other.requirements == requirements &&
        other.rules == rules &&
        other.producerNotificationChannel == producerNotificationChannel &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + requirements.hashCode()
        result = 31 * result + rules.hashCode()
        result = 31 * result + producerNotificationChannel.hashCode()
        return result
    }

    override fun toString(): String = "Usage(" +
        "requirements=$requirements, " +
        "rules=$rules, " +
        "producerNotificationChannel=$producerNotificationChannel" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: UsageDsl.() -> Unit): Usage = Usage.Deserializer {
        requirements = this@Usage.requirements
        rules = this@Usage.rules
        producerNotificationChannel = this@Usage.producerNotificationChannel
        unknownFields = this@Usage.unknownFields
        dsl()
    }

    class UsageDsl {
        var requirements: List<String> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var rules: List<UsageRule> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var producerNotificationChannel: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Usage = Usage(finishList(requirements),
        finishList(rules),
        producerNotificationChannel,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<Usage>, (UsageDsl.() -> Unit) -> Usage {
        override fun deserialize(deserializer: KtMessageDeserializer): Usage {
            var requirements : MutableList<String>? = null
            var rules : MutableList<UsageRule>? = null
            var producerNotificationChannel = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Usage(finishList(requirements),
                            finishList(rules),
                            producerNotificationChannel,
                            UnknownFieldSet.from(unknownFields))
                    10 -> requirements = (requirements ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readString())
                                   }
                               }
                    50 -> rules = (rules ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readMessage(com.google.api.UsageRule))
                                   }
                               }
                    58 -> producerNotificationChannel = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: UsageDsl.() -> Unit): Usage = UsageDsl().apply(dsl).build()
    }
}

/**
 * Usage configuration rules for the service.
 *
 *  NOTE: Under development.
 *
 *
 *
 *  Use this rule to configure unregistered calls for the service. Unregistered calls are calls that
 * do not contain consumer project identity. (Example: calls that do not contain an API key). By
 * default, API methods do not allow unregistered calls, and each method call must be identified by a
 * consumer project identity. Use this rule to allow/disallow unregistered calls.
 *
 *  Example of an API that wants to allow unregistered calls for entire service.
 *
 *      usage:       rules:       - selector: "*"         allow_unregistered_calls: true
 *
 *  Example of a method that wants to allow unregistered calls.
 *
 *      usage:       rules:       - selector: "google.example.library.v1.LibraryService.CreateBook"
 *        allow_unregistered_calls: true
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.UsageRule")
class UsageRule private constructor(
    /**
     * Selects the methods to which this rule applies. Use '*' to indicate all methods in all APIs.
     *
     *  Refer to [selector][google.api.DocumentationRule.selector] for syntax details.
     */
    val selector: String,
    /**
     * If true, the selected method allows unregistered calls, e.g. calls that don't identify any
     * user or application.
     */
    val allowUnregisteredCalls: Boolean,
    /**
     * If true, the selected method should skip service control and the control plane features, such
     * as quota and billing, will not be available. This flag is used by Google Cloud Endpoints to
     * bypass checks for internal methods, such as service health check methods.
     */
    val skipServiceControl: Boolean,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (selector.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(selector)
        }
        if (allowUnregisteredCalls) {
            result += sizeof(Tag(2)) + sizeof(allowUnregisteredCalls)
        }
        if (skipServiceControl) {
            result += sizeof(Tag(3)) + sizeof(skipServiceControl)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (selector.isNotEmpty()) {
            serializer.write(Tag(10)).write(selector)
        }
        if (allowUnregisteredCalls) {
            serializer.write(Tag(16)).write(allowUnregisteredCalls)
        }
        if (skipServiceControl) {
            serializer.write(Tag(24)).write(skipServiceControl)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is UsageRule &&
        other.selector == selector &&
        other.allowUnregisteredCalls == allowUnregisteredCalls &&
        other.skipServiceControl == skipServiceControl &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + selector.hashCode()
        result = 31 * result + allowUnregisteredCalls.hashCode()
        result = 31 * result + skipServiceControl.hashCode()
        return result
    }

    override fun toString(): String = "UsageRule(" +
        "selector=$selector, " +
        "allowUnregisteredCalls=$allowUnregisteredCalls, " +
        "skipServiceControl=$skipServiceControl" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: UsageRuleDsl.() -> Unit): UsageRule = UsageRule.Deserializer {
        selector = this@UsageRule.selector
        allowUnregisteredCalls = this@UsageRule.allowUnregisteredCalls
        skipServiceControl = this@UsageRule.skipServiceControl
        unknownFields = this@UsageRule.unknownFields
        dsl()
    }

    class UsageRuleDsl {
        var selector: String = ""

        var allowUnregisteredCalls: Boolean = false

        var skipServiceControl: Boolean = false

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): UsageRule = UsageRule(selector,
        allowUnregisteredCalls,
        skipServiceControl,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<UsageRule>,
            (UsageRuleDsl.() -> Unit) -> UsageRule {
        override fun deserialize(deserializer: KtMessageDeserializer): UsageRule {
            var selector = ""
            var allowUnregisteredCalls = false
            var skipServiceControl = false
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return UsageRule(selector,
                            allowUnregisteredCalls,
                            skipServiceControl,
                            UnknownFieldSet.from(unknownFields))
                    10 -> selector = deserializer.readString()
                    16 -> allowUnregisteredCalls = deserializer.readBool()
                    24 -> skipServiceControl = deserializer.readBool()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: UsageRuleDsl.() -> Unit): UsageRule =
                UsageRuleDsl().apply(dsl).build()
    }
}
