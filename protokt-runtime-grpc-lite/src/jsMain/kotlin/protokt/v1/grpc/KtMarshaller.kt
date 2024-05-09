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

package protokt.v1.grpc

import protokt.v1.Beta
import protokt.v1.Deserializer
import protokt.v1.Message

@Beta
class KtMarshaller<T : Message>(
    private val deserializer: Deserializer<T>
) : MethodDescriptor.Marshaller<T> {
    override fun parse(bytes: ByteArray) =
        deserializer.deserialize(bytes)

    override fun serialize(value: T): dynamic =
        value.serialize()
}
