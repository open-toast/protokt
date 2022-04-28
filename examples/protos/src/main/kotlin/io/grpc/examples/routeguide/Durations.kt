/*
 * Copyright (c) 2021 Toast Inc.
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

package io.grpc.examples.routeguide

import protokt.com.google.protobuf.Duration

private const val MICROS_PER_SECOND = 1000000
private const val NANOS_PER_MICROSECOND = 1000

object Durations {
    fun fromMicros(microseconds: Long) =
        Duration {
            seconds = microseconds / MICROS_PER_SECOND
            nanos = (microseconds % MICROS_PER_SECOND * NANOS_PER_MICROSECOND).toInt()
        }
}
