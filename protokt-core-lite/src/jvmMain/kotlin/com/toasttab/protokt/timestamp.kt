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

@file:Suppress("DEPRECATION")

// Generated by protokt version 0.12.1. Do not modify.
// Source: google/protobuf/timestamp.proto
package com.toasttab.protokt

import com.toasttab.protokt.rt.Int32
import com.toasttab.protokt.rt.Int64
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
import kotlin.Long
import kotlin.String
import kotlin.Unit

/**
 * A Timestamp represents a point in time independent of any time zone or local calendar, encoded as
 * a count of seconds and fractions of seconds at nanosecond resolution. The count is relative to an
 * epoch at UTC midnight on January 1, 1970, in the proleptic Gregorian calendar which extends the
 * Gregorian calendar backwards to year one.
 *
 *  All minutes are 60 seconds long. Leap seconds are "smeared" so that no leap second table is
 * needed for interpretation, using a [24-hour linear smear](https://developers.google.com/time/smear).
 *
 *
 *  The range is from 0001-01-01T00:00:00Z to 9999-12-31T23:59:59.999999999Z. By restricting to that
 * range, we ensure that we can convert to and from [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)
 * date strings.
 *
 *  # Examples
 *
 *  Example 1: Compute Timestamp from POSIX `time()`.
 *
 *      Timestamp timestamp;     timestamp.set_seconds(time(NULL));     timestamp.set_nanos(0);
 *
 *  Example 2: Compute Timestamp from POSIX `gettimeofday()`.
 *
 *      struct timeval tv;     gettimeofday(&tv, NULL);
 *
 *      Timestamp timestamp;     timestamp.set_seconds(tv.tv_sec);
 * timestamp.set_nanos(tv.tv_usec * 1000);
 *
 *  Example 3: Compute Timestamp from Win32 `GetSystemTimeAsFileTime()`.
 *
 *      FILETIME ft;     GetSystemTimeAsFileTime(&ft);     UINT64 ticks =
 * (((UINT64)ft.dwHighDateTime) << 32) | ft.dwLowDateTime;
 *
 *      // A Windows tick is 100 nanoseconds. Windows epoch 1601-01-01T00:00:00Z     // is
 * 11644473600 seconds before Unix epoch 1970-01-01T00:00:00Z.     Timestamp timestamp;
 * timestamp.set_seconds((INT64) ((ticks / 10000000) - 11644473600LL));     timestamp.set_nanos((INT32)
 * ((ticks % 10000000) * 100));
 *
 *  Example 4: Compute Timestamp from Java `System.currentTimeMillis()`.
 *
 *      long millis = System.currentTimeMillis();
 *
 *      Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
 * .setNanos((int) ((millis % 1000) * 1000000)).build();
 *
 *
 *
 *  Example 5: Compute Timestamp from Java `Instant.now()`.
 *
 *      Instant now = Instant.now();
 *
 *      Timestamp timestamp =         Timestamp.newBuilder().setSeconds(now.getEpochSecond())
 *      .setNanos(now.getNano()).build();
 *
 *
 *
 *  Example 6: Compute Timestamp from current time in Python.
 *
 *      timestamp = Timestamp()     timestamp.GetCurrentTime()
 *
 *  # JSON Mapping
 *
 *  In JSON format, the Timestamp type is encoded as a string in the [RFC
 * 3339](https://www.ietf.org/rfc/rfc3339.txt) format. That is, the format is
 * "{year}-{month}-{day}T{hour}:{min}:{sec}[.{frac_sec}]Z" where {year} is always expressed using four
 * digits while {month}, {day}, {hour}, {min}, and {sec} are zero-padded to two digits each. The
 * fractional seconds, which can go up to 9 digits (i.e. up to 1 nanosecond resolution), are optional.
 * The "Z" suffix indicates the timezone ("UTC"); the timezone is required. A proto3 JSON serializer
 * should always use UTC (as indicated by "Z") when printing the Timestamp type and a proto3 JSON
 * parser should be able to accept both UTC and other timezones (as indicated by an offset).
 *
 *  For example, "2017-01-15T01:30:15.01Z" encodes 15.01 seconds past 01:30 UTC on January 15, 2017.
 *
 *
 *  In JavaScript, one can convert a Date object to this format using the standard
 * [toISOString()](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toISOString)
 * method. In Python, a standard `datetime.datetime` object can be converted to this format using
 * [`strftime`](https://docs.python.org/2/library/time.html#time.strftime) with the time format spec
 * '%Y-%m-%dT%H:%M:%S.%fZ'. Likewise, in Java, one can use the Joda Time's
 * [`ISODateTimeFormat.dateTime()`](
 * http://www.joda.org/joda-time/apidocs/org/joda/time/format/ISODateTimeFormat.html#dateTime%2D%2D )
 * to obtain a formatter capable of generating timestamps in this format.
 *
 *
 */
@Deprecated("for backwards compatibility only")
@KtGeneratedMessage("google.protobuf.Timestamp")
class Timestamp private constructor(
    /**
     * Represents seconds of UTC time since Unix epoch 1970-01-01T00:00:00Z. Must be from
     * 0001-01-01T00:00:00Z to 9999-12-31T23:59:59Z inclusive.
     */
    val seconds: Long,
    /**
     * Non-negative fractions of a second at nanosecond resolution. Negative second values with
     * fractions must still have non-negative nanos values that count forward in time. Must be from 0
     * to 999,999,999 inclusive.
     */
    val nanos: Int,
    val unknownFields: UnknownFieldSet = UnknownFieldSet.empty(),
) : KtMessage {
    override val messageSize: Int by lazy { messageSize() }

    private fun messageSize(): Int {
        var result = 0
        if (seconds != 0L) {
            result += sizeof(Tag(1)) + sizeof(Int64(seconds))
        }
        if (nanos != 0) {
            result += sizeof(Tag(2)) + sizeof(Int32(nanos))
        }
        result += unknownFields.size()
        return result
    }

    override fun serialize(serializer: KtMessageSerializer) {
        if (seconds != 0L) {
            serializer.write(Tag(8)).write(Int64(seconds))
        }
        if (nanos != 0) {
            serializer.write(Tag(16)).write(Int32(nanos))
        }
        serializer.writeUnknown(unknownFields)
    }

    override fun equals(other: Any?): Boolean = other is Timestamp &&
            other.seconds == seconds &&
            other.nanos == nanos &&
            other.unknownFields == unknownFields

    override fun hashCode(): Int {
        var result = unknownFields.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nanos.hashCode()
        return result
    }

    override fun toString(): String = "Timestamp(" +
            "seconds=$seconds, " +
            "nanos=$nanos" +
            "${if (unknownFields.isEmpty()) "" else ", unknownFields=$unknownFields"})"

    fun copy(dsl: TimestampDsl.() -> Unit): Timestamp = Timestamp.Deserializer {
        seconds = this@Timestamp.seconds
        nanos = this@Timestamp.nanos
        unknownFields = this@Timestamp.unknownFields
        dsl()
    }

    class TimestampDsl {
        var seconds: Long = 0L

        var nanos: Int = 0

        var unknownFields: UnknownFieldSet = UnknownFieldSet.empty()

        fun build(): Timestamp = Timestamp(seconds,
            nanos,
            unknownFields)
    }

    companion object Deserializer : KtDeserializer<Timestamp>,
            (TimestampDsl.() -> Unit) -> Timestamp {
        override fun deserialize(deserializer: KtMessageDeserializer): Timestamp {
            var seconds = 0L
            var nanos = 0
            var unknownFields: UnknownFieldSet.Builder? = null
            while (true) {
                when(deserializer.readTag()) {
                    0 -> return Timestamp(seconds,
                        nanos,
                        UnknownFieldSet.from(unknownFields))
                    8 -> seconds = deserializer.readInt64()
                    16 -> nanos = deserializer.readInt32()
                    else -> unknownFields = (unknownFields ?:
                    UnknownFieldSet.Builder()).also {it.add(deserializer.readUnknown()) }
                }
            }
        }

        override fun invoke(dsl: TimestampDsl.() -> Unit): Timestamp =
            TimestampDsl().apply(dsl).build()
    }
}
