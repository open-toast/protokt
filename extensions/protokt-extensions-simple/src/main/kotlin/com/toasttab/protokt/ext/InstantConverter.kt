/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.ext

import com.google.auto.service.AutoService
import protokt.com.google.protobuf.Timestamp
import java.time.Instant

@AutoService(Converter::class)
object InstantConverter : Converter<Instant, Timestamp> {
    override val wrapper = Instant::class

    override val wrapped = Timestamp::class

    override fun wrap(unwrapped: Timestamp): Instant =
        Instant.ofEpochSecond(unwrapped.seconds, unwrapped.nanos.toLong())

    override fun unwrap(wrapped: Instant) =
        Timestamp {
            seconds = wrapped.epochSecond
            nanos = wrapped.nano
        }
}
