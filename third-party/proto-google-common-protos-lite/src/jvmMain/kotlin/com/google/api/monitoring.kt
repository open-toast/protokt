@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/monitoring.proto
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
 * Monitoring configuration of the service.
 *
 *  The example below shows how to configure monitored resources and metrics for monitoring. In the
 * example, a monitored resource and two metrics are defined. The
 * `library.googleapis.com/book/returned_count` metric is sent to both producer and consumer projects,
 * whereas the `library.googleapis.com/book/num_overdue` metric is only sent to the consumer project.
 *
 *      monitored_resources:     - type: library.googleapis.com/Branch       display_name: "Library
 * Branch"       description: "A branch of a library."       launch_stage: GA       labels:       -
 * key: resource_container         description: "The Cloud container (ie. project id) for the
 * Branch."       - key: location         description: "The location of the library branch."       -
 * key: branch_id         description: "The id of the branch."     metrics:     - name:
 * library.googleapis.com/book/returned_count       display_name: "Books Returned"       description:
 * "The count of books that have been returned."       launch_stage: GA       metric_kind: DELTA
 * value_type: INT64       unit: "1"       labels:       - key: customer_id         description: "The
 * id of the customer."     - name: library.googleapis.com/book/num_overdue       display_name: "Books
 * Overdue"       description: "The current number of overdue books."       launch_stage: GA
 * metric_kind: GAUGE       value_type: INT64       unit: "1"       labels:       - key: customer_id
 *      description: "The id of the customer."     monitoring:       producer_destinations:       -
 * monitored_resource: library.googleapis.com/Branch         metrics:         -
 * library.googleapis.com/book/returned_count       consumer_destinations:       - monitored_resource:
 * library.googleapis.com/Branch         metrics:         -
 * library.googleapis.com/book/returned_count         - library.googleapis.com/book/num_overdue
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.api.Monitoring")
class Monitoring private constructor(
    /**
     * Monitoring configurations for sending metrics to the producer project. There can be multiple
     * producer destinations. A monitored resource type may appear in multiple monitoring destinations
     * if different aggregations are needed for different sets of metrics associated with that
     * monitored resource type. A monitored resource and metric pair may only be used once in the
     * Monitoring configuration.
     */
    val producerDestinations: List<MonitoringDestination>,
    /**
     * Monitoring configurations for sending metrics to the consumer project. There can be multiple
     * consumer destinations. A monitored resource type may appear in multiple monitoring destinations
     * if different aggregations are needed for different sets of metrics associated with that
     * monitored resource type. A monitored resource and metric pair may only be used once in the
     * Monitoring configuration.
     */
    val consumerDestinations: List<MonitoringDestination>,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (producerDestinations.isNotEmpty()) {
            result += (sizeof(Tag(1)) * producerDestinations.size) + producerDestinations.sumOf {
                    sizeof(it) }
        }
        if (consumerDestinations.isNotEmpty()) {
            result += (sizeof(Tag(2)) * consumerDestinations.size) + consumerDestinations.sumOf {
                    sizeof(it) }
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (producerDestinations.isNotEmpty()) {
            producerDestinations.forEach { serializer.write(Tag(10)).write(it) }
        }
        if (consumerDestinations.isNotEmpty()) {
            consumerDestinations.forEach { serializer.write(Tag(18)).write(it) }
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Monitoring &&
        other.producerDestinations == producerDestinations &&
        other.consumerDestinations == consumerDestinations &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + producerDestinations.hashCode()
        result = 31 * result + consumerDestinations.hashCode()
        return result
    }

    override fun toString(): String = "Monitoring(" +
        "producerDestinations=$producerDestinations, " +
        "consumerDestinations=$consumerDestinations" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: MonitoringDsl.() -> Unit): Monitoring = Monitoring.Deserializer {
        producerDestinations = this@Monitoring.producerDestinations
        consumerDestinations = this@Monitoring.consumerDestinations
        unknownFields = this@Monitoring.unknownFields
        dsl()
    }

    class MonitoringDsl {
        var producerDestinations: List<MonitoringDestination> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var consumerDestinations: List<MonitoringDestination> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Monitoring = Monitoring(finishList(producerDestinations),
        finishList(consumerDestinations),
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<Monitoring>,
            (MonitoringDsl.() -> Unit) -> Monitoring {
        override fun deserialize(deserializer: KtMessageDeserializer): Monitoring {
            var producerDestinations : MutableList<MonitoringDestination>? = null
            var consumerDestinations : MutableList<MonitoringDestination>? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Monitoring(finishList(producerDestinations),
                            finishList(consumerDestinations),
                            UnknownFieldSet.from(unknownFields))
                    10 -> producerDestinations = (producerDestinations ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {

                                    add(deserializer.readMessage(com.google.api.Monitoring.MonitoringDestination))
                                   }
                               }
                    18 -> consumerDestinations = (consumerDestinations ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {

                                    add(deserializer.readMessage(com.google.api.Monitoring.MonitoringDestination))
                                   }
                               }
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: MonitoringDsl.() -> Unit): Monitoring =
                MonitoringDsl().apply(dsl).build()
    }

    /**
     * Configuration of a specific monitoring destination (the producer project or the consumer
     * project).
     */
    @KtGeneratedMessage("google.api.MonitoringDestination")
    class MonitoringDestination private constructor(
        /**
         * The monitored resource type. The type must be defined in
         * [Service.monitored_resources][google.api.Service.monitored_resources] section.
         */
        val monitoredResource: String,
        /**
         * Types of the metrics to report to this monitoring destination. Each type must be defined
         * in [Service.metrics][google.api.Service.metrics] section.
         */
        val metrics: List<String>,
        val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
    ) : KtMessage {
        override val messageSize: Int by lazy { messageSize() }

        private fun messageSize(): Int {
            var result = 0
            if (monitoredResource.isNotEmpty()) {
                result += sizeof(Tag(1)) + sizeof(monitoredResource)
            }
            if (metrics.isNotEmpty()) {
                result += (sizeof(Tag(2)) * metrics.size) + metrics.sumOf { sizeof(it) }
            }
            result += unknownFields.size()
            return result
        }

        override fun serialize(serializer: KtMessageSerializer) {
            if (monitoredResource.isNotEmpty()) {
                serializer.write(Tag(10)).write(monitoredResource)
            }
            if (metrics.isNotEmpty()) {
                metrics.forEach { serializer.write(Tag(18)).write(it) }
            }
            serializer.writeUnknown(unknownFields)
        }

        override fun equals(other: Any?): Boolean = other is MonitoringDestination &&
            other.monitoredResource == monitoredResource &&
            other.metrics == metrics &&
            other.unknownFields == unknownFields

        override fun hashCode(): Int {
            var result = unknownFields.hashCode()
            result = 31 * result + monitoredResource.hashCode()
            result = 31 * result + metrics.hashCode()
            return result
        }

        override fun toString(): String = "MonitoringDestination(" +
            "monitoredResource=$monitoredResource, " +
            "metrics=$metrics" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

        fun copy(dsl: MonitoringDestinationDsl.() -> Unit): MonitoringDestination =
                MonitoringDestination.Deserializer {
            monitoredResource = this@MonitoringDestination.monitoredResource
            metrics = this@MonitoringDestination.metrics
            unknownFields = this@MonitoringDestination.unknownFields
            dsl()
        }

        class MonitoringDestinationDsl {
            var monitoredResource: String = ""

            var metrics: List<String> = emptyList()
                set(newValue) {
                    field = copyList(newValue)
                }

            var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

            fun build(): MonitoringDestination = MonitoringDestination(monitoredResource,
            finishList(metrics),
             unknownFields)
        }

        companion object Deserializer : KtDeserializer<MonitoringDestination>,
                (MonitoringDestinationDsl.() -> Unit) -> MonitoringDestination {
            override fun deserialize(deserializer: KtMessageDeserializer):
                    MonitoringDestination {
                var monitoredResource = ""
                var metrics : MutableList<String>? = null
                var unknownFields: UnknownFieldSet.Builder? = null
                while (true) {
                    when(deserializer.readTag()) {
                        0 -> return MonitoringDestination(monitoredResource,
                                finishList(metrics),
                                UnknownFieldSet.from(unknownFields))
                        10 -> monitoredResource = deserializer.readString()
                        18 -> metrics = (metrics ?: mutableListOf()).apply {
                                       deserializer.readRepeated(false) {
                                           add(deserializer.readString())
                                       }
                                   }
                        else -> unknownFields = (unknownFields ?:
                                UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown())
                                }
                    }
                }
            }

            override fun invoke(dsl: MonitoringDestinationDsl.() -> Unit):
                    MonitoringDestination = MonitoringDestinationDsl().apply(dsl).build()
        }
    }
}
