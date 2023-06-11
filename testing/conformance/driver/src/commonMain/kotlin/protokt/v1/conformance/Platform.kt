/*
 * Copyright (c) 2022 Toast, Inc.
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

package protokt.v1.conformance

import kotlinx.coroutines.CoroutineScope
import protokt.v1.Bytes
import protokt.v1.KtDeserializer
import protokt.v1.KtMessage

internal expect object Platform {
    fun printErr(message: String)

    fun runBlockingMain(block: suspend CoroutineScope.() -> Unit)

    suspend fun <T : KtMessage> readMessageFromStdIn(
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>?

    fun writeToStdOut(bytes: ByteArray)

    fun <T : KtMessage> deserialize(
        bytes: Bytes,
        deserializer: KtDeserializer<T>
    ): ConformanceStepResult<T>

    fun serialize(message: KtMessage): ConformanceStepResult<ByteArray>
}
