@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/geo/type/viewport.proto
package com.google.geo.type

import com.google.type.LatLng
import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtGeneratedMessage
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageDeserializer
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.protokt.rt.Tag
import com.toasttab.protokt.rt.UnknownFieldSet
import com.toasttab.protokt.rt.sizeof
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit

/**
 * A latitude-longitude viewport, represented as two diagonally opposite `low` and `high` points. A
 * viewport is considered a closed region, i.e. it includes its boundary. The latitude bounds must
 * range between -90 to 90 degrees inclusive, and the longitude bounds must range between -180 to 180
 * degrees inclusive. Various cases include:
 *
 *   - If `low` = `high`, the viewport consists of that single point.
 *
 *   - If `low.longitude` > `high.longitude`, the longitude range is inverted    (the viewport
 * crosses the 180 degree longitude line).
 *
 *   - If `low.longitude` = -180 degrees and `high.longitude` = 180 degrees,    the viewport
 * includes all longitudes.
 *
 *   - If `low.longitude` = 180 degrees and `high.longitude` = -180 degrees,    the longitude range
 * is empty.
 *
 *   - If `low.latitude` > `high.latitude`, the latitude range is empty.
 *
 *  Both `low` and `high` must be populated, and the represented box cannot be empty (as specified
 * by the definitions above). An empty viewport will result in an error.
 *
 *  For example, this viewport fully encloses New York City:
 *
 *  {     "low": {         "latitude": 40.477398,         "longitude": -74.259087     },     "high":
 * {         "latitude": 40.91618,         "longitude": -73.70018     } }
 */
@Deprecated("use v1")
@KtGeneratedMessage("google.geo.type.Viewport")
class Viewport private constructor(
    /**
     * Required. The low point of the viewport.
     */
    val low: LatLng?,
    /**
     * Required. The high point of the viewport.
     */
    val high: LatLng?,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (low  != null) {
            result += sizeof(Tag(1)) + sizeof(low)
        }
        if (high  != null) {
            result += sizeof(Tag(2)) + sizeof(high)
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (low  != null) {
            serializer.write(Tag(10)).write(low)
        }
        if (high  != null) {
            serializer.write(Tag(18)).write(high)
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Viewport &&
        other.low == low &&
        other.high == high &&
        other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + low.hashCode()
        result = 31 * result + high.hashCode()
        return result
    }

    override fun toString(): String = "Viewport(" +
        "low=$low, " +
        "high=$high" +
        "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: ViewportDsl.() -> Unit): Viewport = Viewport.Deserializer {
        low = this@Viewport.low
        high = this@Viewport.high
        unknownFields = this@Viewport.unknownFields
        dsl()
    }

    class ViewportDsl {
        var low: LatLng? = null

        var high: LatLng? = null

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Viewport = Viewport(low,
        high,
         unknownFields)
    }

    companion object Deserializer : KtDeserializer<Viewport>,
            (ViewportDsl.() -> Unit) -> Viewport {
        override fun deserialize(deserializer: KtMessageDeserializer): Viewport {
            var low : LatLng? = null
            var high : LatLng? = null
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Viewport(low,
                            high,
                            UnknownFieldSet.from(unknownFields))
                    10 -> low = deserializer.readMessage(com.google.type.LatLng)
                    18 -> high = deserializer.readMessage(com.google.type.LatLng)
                    else -> unknownFields = (unknownFields ?:
                            UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: ViewportDsl.() -> Unit): Viewport =
                ViewportDsl().apply(dsl).build()
    }
}
