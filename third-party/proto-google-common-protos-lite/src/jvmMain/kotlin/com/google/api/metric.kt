@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/api/metric.proto
package com.google.api

import com.toasttab.protokt.Duration
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.copyList
import com.toasttab.protokt.rt.copyMap
import com.toasttab.protokt.rt.finishList
import com.toasttab.protokt.rt.finishMap
import com.toasttab.protokt.rt.sizeof
import com.toasttab.protokt.rt.sizeofMap
import kotlin.Any
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap

/**
 * Defines a metric type and its schema. Once a metric descriptor is created, deleting or altering
 * it stops data collection and makes the metric type's existing data unusable.
 */
@KtGeneratedMessage("google.api.MetricDescriptor")
@Suppress("DEPRECATION")
class MetricDescriptor private constructor(
    /**
     * The resource name of the metric descriptor.
     */
    val name: String,
    /**
     * The set of labels that can be used to describe a specific instance of this metric type. For
     * example, the `appengine.googleapis.com/http/server/response_latencies` metric type has a label
     * for the HTTP response code, `response_code`, so you can look at latencies for successful
     * responses or just for responses that failed.
     */
    val labels: List<LabelDescriptor>,
    /**
     * Whether the metric records instantaneous values, changes to a value, etc. Some combinations
     * of `metric_kind` and `value_type` might not be supported.
     */
    val metricKind: MetricKind,
    /**
     * Whether the measurement is an integer, a floating-point number, etc. Some combinations of
     * `metric_kind` and `value_type` might not be supported.
     */
    val valueType: ValueType,
    /**
     * The units in which the metric value is reported. It is only applicable if the `value_type` is
     * `INT64`, `DOUBLE`, or `DISTRIBUTION`. The `unit` defines the representation of the stored metric
     * values.
     *
     *  Different systems might scale the values to be more easily displayed (so a value of
     * `0.02kBy` _might_ be displayed as `20By`, and a value of `3523kBy` _might_ be displayed as
     * `3.5MBy`). However, if the `unit` is `kBy`, then the value of the metric is always in thousands
     * of bytes, no matter how it might be displayed.
     *
     *  If you want a custom metric to record the exact number of CPU-seconds used by a job, you can
     * create an `INT64 CUMULATIVE` metric whose `unit` is `s{CPU}` (or equivalently `1s{CPU}` or just
     * `s`). If the job uses 12,005 CPU-seconds, then the value is written as `12005`.
     *
     *  Alternatively, if you want a custom metric to record data in a more granular way, you can
     * create a `DOUBLE CUMULATIVE` metric whose `unit` is `ks{CPU}`, and then write the value `12.005`
     * (which is `12005/1000`), or use `Kis{CPU}` and write `11.723` (which is `12005/1024`).
     *
     *  The supported units are a subset of [The Unified Code for Units of
     * Measure](https://unitsofmeasure.org/ucum.html) standard:
     *
     *  **Basic units (UNIT)**
     *
     *  * `bit`   bit * `By`    byte * `s`     second * `min`   minute * `h`     hour * `d`     day
     * * `1`     dimensionless
     *
     *  **Prefixes (PREFIX)**
     *
     *  * `k`     kilo    (10^3) * `M`     mega    (10^6) * `G`     giga    (10^9) * `T`     tera
     * (10^12) * `P`     peta    (10^15) * `E`     exa     (10^18) * `Z`     zetta   (10^21) * `Y`
     * yotta   (10^24)
     *
     *  * `m`     milli   (10^-3) * `u`     micro   (10^-6) * `n`     nano    (10^-9) * `p`     pico
     *    (10^-12) * `f`     femto   (10^-15) * `a`     atto    (10^-18) * `z`     zepto   (10^-21) *
     * `y`     yocto   (10^-24)
     *
     *  * `Ki`    kibi    (2^10) * `Mi`    mebi    (2^20) * `Gi`    gibi    (2^30) * `Ti`    tebi
     * (2^40) * `Pi`    pebi    (2^50)
     *
     *  **Grammar**
     *
     *  The grammar also includes these connectors:
     *
     *  * `/`    division or ratio (as an infix operator). For examples,          `kBy/{email}` or
     * `MiBy/10ms` (although you should almost never          have `/s` in a metric `unit`; rates
     * should always be computed at          query time from the underlying cumulative or delta value).
     * * `.`    multiplication or composition (as an infix operator). For          examples, `GBy.d` or
     * `k{watt}.h`.
     *
     *  The grammar for a unit is as follows:
     *
     *      Expression = Component { "." Component } { "/" Component } ;
     *
     *      Component = ( [ PREFIX ] UNIT | "%" ) [ Annotation ]               | Annotation
     *      | "1"               ;
     *
     *      Annotation = "{" NAME "}" ;
     *
     *  Notes:
     *
     *  * `Annotation` is just a comment if it follows a `UNIT`. If the annotation    is used alone,
     * then the unit is equivalent to `1`. For examples,    `{request}/s == 1/s`, `By{transmitted}/s ==
     * By/s`. * `NAME` is a sequence of non-blank printable ASCII characters not    containing `{` or
     * `}`. * `1` represents a unitary [dimensionless
     * unit](https://en.wikipedia.org/wiki/Dimensionless_quantity) of 1, such    as in `1/s`. It is
     * typically used when none of the basic units are    appropriate. For example, "new users per day"
     * can be represented as    `1/d` or `{new-users}/d` (and a metric value `5` would mean "5 new
     * users). Alternatively, "thousands of page views per day" would be    represented as `1000/d` or
     * `k1/d` or `k{page_views}/d` (and a metric    value of `5.3` would mean "5300 page views per
     * day"). * `%` represents dimensionless value of 1/100, and annotates values giving    a
     * percentage (so the metric values are typically in the range of 0..100,    and a metric value `3`
     * means "3 percent"). * `10^2.%` indicates a metric contains a ratio, typically in the range
     * 0..1, that will be multiplied by 100 and displayed as a percentage    (so a metric value `0.03`
     * means "3 percent").
     */
    val unit: String,
    /**
     * A detailed description of the metric, which can be used in documentation.
     */
    val description: String,
    /**
     * A concise name for the metric, which can be displayed in user interfaces. Use sentence case
     * without an ending period, for example "Request count". This field is optional but it is
     * recommended to be set for any metrics associated with user-visible concepts, such as Quota.
     */
    val displayName: String,
    /**
     * The metric type, including its DNS name prefix. The type is not URL-encoded. All user-defined
     * metric types have the DNS name `custom.googleapis.com` or `external.googleapis.com`. Metric
     * types should use a natural hierarchical grouping. For example:
     *
     *      "custom.googleapis.com/invoice/paid/amount"     "external.googleapis.com/prometheus/up"
     *    "appengine.googleapis.com/http/server/response_latencies"
     */
    val type: String,
    /**
     * Optional. Metadata which can be used to guide usage of the metric.
     */
    val metadata: MetricDescriptorMetadata?,
    /**
     * Optional. The launch stage of the metric definition.
     */
    val launchStage: LaunchStage,
    /**
     * Read-only. If present, then a [time series][google.monitoring.v3.TimeSeries], which is
     * identified partially by a metric type and a
     * [MonitoredResourceDescriptor][google.api.MonitoredResourceDescriptor], that is associated with
     * this metric type can only be associated with one of the monitored resource types listed here.
     */
    val monitoredResourceTypes: List<String>,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (name.isNotEmpty()) {
            result += sizeof(Tag(1)) + sizeof(name)
        }
        if (labels.isNotEmpty()) {
            result += (sizeof(Tag(2)) * labels.size) + labels.sumOf { sizeof(it) }
        }
        if (metricKind.value != 0) {
            result += sizeof(Tag(3)) + sizeof(metricKind)
        }
        if (valueType.value != 0) {
            result += sizeof(Tag(4)) + sizeof(valueType)
        }
        if (unit.isNotEmpty()) {
            result += sizeof(Tag(5)) + sizeof(unit)
        }
        if (description.isNotEmpty()) {
            result += sizeof(Tag(6)) + sizeof(description)
        }
        if (displayName.isNotEmpty()) {
            result += sizeof(Tag(7)) + sizeof(displayName)
        }
        if (type.isNotEmpty()) {
            result += sizeof(Tag(8)) + sizeof(type)
        }
        if (metadata  != null) {
            result += sizeof(Tag(10)) + sizeof(metadata)
        }
        if (launchStage.value != 0) {
            result += sizeof(Tag(12)) + sizeof(launchStage)
        }
        if (monitoredResourceTypes.isNotEmpty()) {
            result += (sizeof(Tag(13)) * monitoredResourceTypes.size) +
                    monitoredResourceTypes.sumOf { sizeof(it) }
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (name.isNotEmpty()) {
            serializer.write(Tag(10)).write(name)
        }
        if (labels.isNotEmpty()) {
            labels.forEach { serializer.write(Tag(18)).write(it) }
        }
        if (metricKind.value != 0) {
            serializer.write(Tag(24)).write(metricKind)
        }
        if (valueType.value != 0) {
            serializer.write(Tag(32)).write(valueType)
        }
        if (unit.isNotEmpty()) {
            serializer.write(Tag(42)).write(unit)
        }
        if (description.isNotEmpty()) {
            serializer.write(Tag(50)).write(description)
        }
        if (displayName.isNotEmpty()) {
            serializer.write(Tag(58)).write(displayName)
        }
        if (type.isNotEmpty()) {
            serializer.write(Tag(66)).write(type)
        }
        if (metadata  != null) {
            serializer.write(Tag(82)).write(metadata)
        }
        if (launchStage.value != 0) {
            serializer.write(Tag(96)).write(launchStage)
        }
        if (monitoredResourceTypes.isNotEmpty()) {
            monitoredResourceTypes.forEach { serializer.write(Tag(106)).write(it) }
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is MetricDescriptor &&
        other.name == name &&
        other.labels == labels &&
        other.metricKind == metricKind &&
        other.valueType == valueType &&
        other.unit == unit &&
        other.description == description &&
        other.displayName == displayName &&
        other.type == type &&
        other.metadata == metadata &&
        other.launchStage == launchStage &&
        other.monitoredResourceTypes == monitoredResourceTypes &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + labels.hashCode()
        result = 31 * result + metricKind.hashCode()
        result = 31 * result + valueType.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + launchStage.hashCode()
        result = 31 * result + monitoredResourceTypes.hashCode()
        return result
    }

    override fun toString(): String = "MetricDescriptor(" +
        "name=$name, " +
        "labels=$labels, " +
        "metricKind=$metricKind, " +
        "valueType=$valueType, " +
        "unit=$unit, " +
        "description=$description, " +
        "displayName=$displayName, " +
        "type=$type, " +
        "metadata=$metadata, " +
        "launchStage=$launchStage, " +
        "monitoredResourceTypes=$monitoredResourceTypes" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: MetricDescriptorDsl.() -> Unit): MetricDescriptor =
            MetricDescriptor.Deserializer {
        name = this@MetricDescriptor.name
        labels = this@MetricDescriptor.labels
        metricKind = this@MetricDescriptor.metricKind
        valueType = this@MetricDescriptor.valueType
        unit = this@MetricDescriptor.unit
        description = this@MetricDescriptor.description
        displayName = this@MetricDescriptor.displayName
        type = this@MetricDescriptor.type
        metadata = this@MetricDescriptor.metadata
        launchStage = this@MetricDescriptor.launchStage
        monitoredResourceTypes = this@MetricDescriptor.monitoredResourceTypes
        unknownFields = this@MetricDescriptor.unknownFields
        dsl()
    }

    class MetricDescriptorDsl {
        var name: String = ""

        var labels: List<LabelDescriptor> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var metricKind: MetricKind = MetricKind.from(0)

        var valueType: ValueType = ValueType.from(0)

        var unit: String = ""

        var description: String = ""

        var displayName: String = ""

        var type: String = ""

        var metadata: MetricDescriptorMetadata? = null

        var launchStage: LaunchStage = LaunchStage.from(0)

        var monitoredResourceTypes: List<String> = emptyList()
            set(newValue) {
                field = copyList(newValue)
            }

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): MetricDescriptor = MetricDescriptor(name,
        finishList(labels),
        metricKind,
        valueType,
        unit,
        description,
        displayName,
        type,
        metadata,
        launchStage,
        finishList(monitoredResourceTypes),
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<MetricDescriptor>,
            (MetricDescriptorDsl.() -> Unit) -> MetricDescriptor {
        override fun deserialize(deserializer: KtMessageDeserializer): MetricDescriptor {
            var name = ""
            var labels : MutableList<LabelDescriptor>? = null
            var metricKind = MetricKind.from(0)
            var valueType = ValueType.from(0)
            var unit = ""
            var description = ""
            var displayName = ""
            var type = ""
            var metadata : MetricDescriptorMetadata? = null
            var launchStage = LaunchStage.from(0)
            var monitoredResourceTypes : MutableList<String>? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return MetricDescriptor(name,
                            finishList(labels),
                            metricKind,
                            valueType,
                            unit,
                            description,
                            displayName,
                            type,
                            metadata,
                            launchStage,
                            finishList(monitoredResourceTypes),
                            UnknownFieldSet.from(unknownFields))
                    10 -> name = deserializer.readString()
                    18 -> labels = (labels ?: mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readMessage(com.google.api.LabelDescriptor))
                                   }
                               }
                    24 -> metricKind =
                            deserializer.readEnum(com.google.api.MetricDescriptor.MetricKind)
                    32 -> valueType =
                            deserializer.readEnum(com.google.api.MetricDescriptor.ValueType)
                    42 -> unit = deserializer.readString()
                    50 -> description = deserializer.readString()
                    58 -> displayName = deserializer.readString()
                    66 -> type = deserializer.readString()
                    82 -> metadata =
                            deserializer.readMessage(com.google.api.MetricDescriptor.MetricDescriptorMetadata)
                    96 -> launchStage = deserializer.readEnum(com.google.api.LaunchStage)
                    106 -> monitoredResourceTypes = (monitoredResourceTypes ?:
                            mutableListOf()).apply {
                                   deserializer.readRepeated(false) {
                                       add(deserializer.readString())
                                   }
                               }
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: MetricDescriptorDsl.() -> Unit): MetricDescriptor =
                MetricDescriptorDsl().apply(dsl).build()
    }

    /**
     * The kind of measurement. It describes how the data is reported. For information on setting
     * the start time and end time based on the MetricKind, see
     * [TimeInterval][google.monitoring.v3.TimeInterval].
     */
    sealed class MetricKind(
        override val `value`: Int,
        override val name: String,
    ) : KtEnum() {
        /**
         * Do not use this default value.
         */
        object METRIC_KIND_UNSPECIFIED : MetricKind(0, "METRIC_KIND_UNSPECIFIED")

        /**
         * An instantaneous measurement of a value.
         */
        object GAUGE : MetricKind(1, "GAUGE")

        /**
         * The change in a value during a time interval.
         */
        object DELTA : MetricKind(2, "DELTA")

        /**
         * A value accumulated over a time interval.  Cumulative measurements in a time series
         * should have the same start time and increasing end times, until an event resets the
         * cumulative value to zero and sets a new start time for the following points.
         */
        object CUMULATIVE : MetricKind(3, "CUMULATIVE")

        class UNRECOGNIZED(
            `value`: Int,
        ) : MetricKind(value, "UNRECOGNIZED")

        companion object Deserializer : KtEnumDeserializer<MetricKind> {
            override fun from(`value`: Int): MetricKind = when (value) {
              0 -> METRIC_KIND_UNSPECIFIED
              1 -> GAUGE
              2 -> DELTA
              3 -> CUMULATIVE
              else -> UNRECOGNIZED(value)
            }
        }
    }

    /**
     * The value type of a metric.
     */
    sealed class ValueType(
        override val `value`: Int,
        override val name: String,
    ) : KtEnum() {
        /**
         * Do not use this default value.
         */
        object VALUE_TYPE_UNSPECIFIED : ValueType(0, "VALUE_TYPE_UNSPECIFIED")

        /**
         * The value is a boolean. This value type can be used only if the metric kind is `GAUGE`.
         */
        object BOOL : ValueType(1, "BOOL")

        /**
         * The value is a signed 64-bit integer.
         */
        object INT64 : ValueType(2, "INT64")

        /**
         * The value is a double precision floating point number.
         */
        object DOUBLE : ValueType(3, "DOUBLE")

        /**
         * The value is a text string. This value type can be used only if the metric kind is
         * `GAUGE`.
         */
        object STRING : ValueType(4, "STRING")

        /**
         * The value is a [`Distribution`][google.api.Distribution].
         */
        object DISTRIBUTION : ValueType(5, "DISTRIBUTION")

        /**
         * The value is money.
         */
        object MONEY : ValueType(6, "MONEY")

        class UNRECOGNIZED(
            `value`: Int,
        ) : ValueType(value, "UNRECOGNIZED")

        companion object Deserializer : KtEnumDeserializer<ValueType> {
            override fun from(`value`: Int): ValueType = when (value) {
              0 -> VALUE_TYPE_UNSPECIFIED
              1 -> BOOL
              2 -> INT64
              3 -> DOUBLE
              4 -> STRING
              5 -> DISTRIBUTION
              6 -> MONEY
              else -> UNRECOGNIZED(value)
            }
        }
    }

    /**
     * Additional annotations that can be used to guide the usage of a metric.
     */
    @KtGeneratedMessage("google.api.MetricDescriptorMetadata")
    class MetricDescriptorMetadata private constructor(
        /**
         * Deprecated. Must use the
         * [MetricDescriptor.launch_stage][google.api.MetricDescriptor.launch_stage] instead.
         */
        @Deprecated("deprecated in proto")
        val launchStage: LaunchStage,
        /**
         * The sampling period of metric data points. For metrics which are written periodically,
         * consecutive data points are stored at this time interval, excluding data loss due to errors.
         * Metrics with a higher granularity have a smaller sampling period.
         */
        val samplePeriod: Duration?,
        /**
         * The delay of data points caused by ingestion. Data points older than this age are
         * guaranteed to be ingested and available to be read, excluding data loss due to errors.
         */
        val ingestDelay: Duration?,
        val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
    ) : KtMessage {
        override val messageSize: Int by lazy { messageSize() }

        private fun messageSize(): Int {
            var result = 0
            if (launchStage.value != 0) {
                result += sizeof(Tag(1)) + sizeof(launchStage)
            }
            if (samplePeriod  != null) {
                result += sizeof(Tag(2)) + sizeof(samplePeriod)
            }
            if (ingestDelay  != null) {
                result += sizeof(Tag(3)) + sizeof(ingestDelay)
            }
            result += unknownFields.size()
            return result
        }

        override fun serialize(serializer: KtMessageSerializer) {
            if (launchStage.value != 0) {
                serializer.write(Tag(8)).write(launchStage)
            }
            if (samplePeriod  != null) {
                serializer.write(Tag(18)).write(samplePeriod)
            }
            if (ingestDelay  != null) {
                serializer.write(Tag(26)).write(ingestDelay)
            }
            serializer.writeUnknown(unknownFields)
        }

        override fun equals(other: Any?): Boolean = other is MetricDescriptorMetadata &&
            other.launchStage == launchStage &&
            other.samplePeriod == samplePeriod &&
            other.ingestDelay == ingestDelay &&
            other.unknownFields == unknownFields

        override fun hashCode(): Int {
            var result = unknownFields.hashCode()
            result = 31 * result + launchStage.hashCode()
            result = 31 * result + samplePeriod.hashCode()
            result = 31 * result + ingestDelay.hashCode()
            return result
        }

        override fun toString(): String = "MetricDescriptorMetadata(" +
            "launchStage=$launchStage, " +
            "samplePeriod=$samplePeriod, " +
            "ingestDelay=$ingestDelay" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

        fun copy(dsl: MetricDescriptorMetadataDsl.() -> Unit): MetricDescriptorMetadata =
                MetricDescriptorMetadata.Deserializer {
            launchStage = this@MetricDescriptorMetadata.launchStage
            samplePeriod = this@MetricDescriptorMetadata.samplePeriod
            ingestDelay = this@MetricDescriptorMetadata.ingestDelay
            unknownFields = this@MetricDescriptorMetadata.unknownFields
            dsl()
        }

        class MetricDescriptorMetadataDsl {
            @Deprecated("deprecated in proto")
            var launchStage: LaunchStage = LaunchStage.from(0)

            var samplePeriod: Duration? = null

            var ingestDelay: Duration? = null

            var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

            fun build(): MetricDescriptorMetadata = MetricDescriptorMetadata(launchStage,
            samplePeriod,
            ingestDelay,
             unknownFields)
        }

        companion object Deserializer : KtDeserializer<MetricDescriptorMetadata>,
                (MetricDescriptorMetadataDsl.() -> Unit) -> MetricDescriptorMetadata {
            override fun deserialize(deserializer: KtMessageDeserializer):
                    MetricDescriptorMetadata {
                var launchStage = LaunchStage.from(0)
                var samplePeriod : Duration? = null
                var ingestDelay : Duration? = null
                var unknownFields: UnknownFieldSet.Builder? = null
                while (true) {
                    when(deserializer.readTag()) {
                        0 -> return MetricDescriptorMetadata(launchStage,
                                samplePeriod,
                                ingestDelay,
                                UnknownFieldSet.from(unknownFields))
                        8 -> launchStage = deserializer.readEnum(com.google.api.LaunchStage)
                        18 -> samplePeriod = deserializer.readMessage(com.toasttab.protokt.Duration)
                        26 -> ingestDelay = deserializer.readMessage(com.toasttab.protokt.Duration)
                        else -> unknownFields = (unknownFields ?:
                                UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown())
                                }
                    }
                }
            }

            override fun invoke(dsl: MetricDescriptorMetadataDsl.() -> Unit):
                    MetricDescriptorMetadata = MetricDescriptorMetadataDsl().apply(dsl).build()
        }
    }
}

/**
 * A specific metric, identified by specifying values for all of the labels of a
 * [`MetricDescriptor`][google.api.MetricDescriptor].
 */
@KtGeneratedMessage("google.api.Metric")
class Metric private constructor(
    /**
     * The set of label values that uniquely identify this metric. All labels listed in the
     * `MetricDescriptor` must be assigned values.
     */
    val labels: Map<String, String>,
    /**
     * An existing metric type, see [google.api.MetricDescriptor][google.api.MetricDescriptor]. For
     * example, `custom.googleapis.com/invoice/paid/amount`.
     */
    val type: String,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (labels.isNotEmpty()) {
            result += sizeofMap(labels, Tag(2)) { k, v -> LabelsEntry.sizeof(k, v)}
        }
        if (type.isNotEmpty()) {
            result += sizeof(Tag(3)) + sizeof(type)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (labels.isNotEmpty()) {
            labels.entries.forEach { serializer.write(Tag(18)).write(LabelsEntry(it.key, it.value))
                    }
        }
        if (type.isNotEmpty()) {
            serializer.write(Tag(26)).write(type)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Metric &&
        other.labels == labels &&
        other.type == type &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + labels.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString(): String = "Metric(" +
        "labels=$labels, " +
        "type=$type" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: MetricDsl.() -> Unit): Metric = Metric.Deserializer {
        labels = this@Metric.labels
        type = this@Metric.type
        unknownFields = this@Metric.unknownFields
        dsl()
    }

    class MetricDsl {
        var labels: Map<String, String> = emptyMap()
            set(newValue) {
                field = copyMap(newValue)
            }

        var type: String = ""

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Metric = Metric(finishMap(labels),
        type,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<Metric>, (MetricDsl.() -> Unit) -> Metric
            {
        override fun deserialize(deserializer: KtMessageDeserializer): Metric {
            var labels : MutableMap<String, String>? = null
            var type = ""
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Metric(finishMap(labels),
                            type,
                            UnknownFieldSet.from(unknownFields))
                    18 -> labels = (labels ?: mutableMapOf()).apply {
                                   deserializer.readRepeated(false) {
                                       deserializer.readMessage(com.google.api.Metric.LabelsEntry)
                                       .let { put(
                                           it.key,
                                           it.value
                                       ) }
                                   }
                               }
                    26 -> type = deserializer.readString()
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: MetricDsl.() -> Unit): Metric =
                MetricDsl().apply(dsl).build()
    }

    private class LabelsEntry(
        val key: String,
        val `value`: String,
    ) : KtMessage {
        override val messageSize: Int
            get() = sizeof(key, value)

        override fun serialize(serializer: KtMessageSerializer) {
            serializer.write(Tag(10)).write(key)

            serializer.write(Tag(18)).write(value)
        }

        companion object Deserializer : KtDeserializer<LabelsEntry> {
            fun sizeof(key: String, `value`: String) =
                    com.toasttab.protokt.rt.sizeof(com.toasttab.protokt.rt.Tag(1)) + com.toasttab.protokt.rt.sizeof(key) + com.toasttab.protokt.rt.sizeof(com.toasttab.protokt.rt.Tag(2)) + com.toasttab.protokt.rt.sizeof(value)

            override fun deserialize(deserializer: KtMessageDeserializer): LabelsEntry {
                var key = ""
                var value = ""

                while (true) {
                  when (deserializer.readTag()) {
                    0 -> return LabelsEntry(key, value)
                    10 -> key = deserializer.readString()
                    18 -> value = deserializer.readString()
                  }
                }
            }
        }
    }
}
