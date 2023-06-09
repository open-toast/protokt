/*
 * Copyright (c) 2020 Toast, Inc.
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

package com.toasttab.protokt.grpc

import protokt.v1.KtDeserializer
import io.grpc.MethodDescriptor
import java.io.InputStream

@Suppress("DEPRECATION")
@Deprecated("use v1 package")
class KtMarshaller<T : com.toasttab.protokt.rt.KtMessage> private constructor(
    private val new: KtDeserializer<T>?,
    private val old: com.toasttab.protokt.rt.KtDeserializer<T>?
) : MethodDescriptor.Marshaller<T> {
    constructor(new: KtDeserializer<T>) : this(new, null)

    @Deprecated("for backwards compatibility only")
    constructor(old: com.toasttab.protokt.rt.KtDeserializer<T>) : this(null, old)

    override fun stream(value: T) =
        value.serialize().inputStream()

    override fun parse(stream: InputStream) =
        new?.deserialize(stream) ?: old!!.deserialize(stream)
}
