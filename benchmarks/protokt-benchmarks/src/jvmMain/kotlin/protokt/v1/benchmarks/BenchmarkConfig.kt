/*
 * Copyright (c) 2026 Toast, Inc.
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

package protokt.v1.benchmarks

import protokt.v1.KotlinxIoCodec
import protokt.v1.PersistentCollectionFactory
import protokt.v1.ProtobufJavaCodec
import protokt.v1.ProtoktRuntime

actual fun applyBenchmarkConfig(collectionFactory: String, codec: String) {
    val configuration = collectionFactory to codec
    check(appliedConfiguration == null || appliedConfiguration == configuration) {
        "Benchmark runtime is already configured as $appliedConfiguration"
    }
    if (appliedConfiguration != null) {
        return
    }
    appliedConfiguration = configuration

    val selectedCodec =
        when (codec) {
            "protokt.v1.ProtobufJavaCodec" -> ProtobufJavaCodec
            "protokt.v1.KotlinxIoCodec" -> KotlinxIoCodec
            else -> null
        }
    val persistent = collectionFactory == "protokt.v1.PersistentCollectionFactory"
    when {
        selectedCodec != null && persistent -> ProtoktRuntime.configure(selectedCodec, PersistentCollectionFactory)
        selectedCodec != null -> ProtoktRuntime.configure(selectedCodec)
        persistent -> ProtoktRuntime.configure(PersistentCollectionFactory)
    }
}

private var appliedConfiguration: Pair<String, String>? = null
