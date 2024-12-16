/*
 * Copyright (c) 2024 Toast, Inc.
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

package protokt.v1.buf.validate.conformance

import com.google.protobuf.ByteString
import io.github.classgraph.ClassGraph
import protokt.v1.Deserializer
import protokt.v1.GeneratedMessage
import protokt.v1.Message
import protokt.v1.google.protobuf.Empty

object DynamicConcreteMessageDeserializer {
    private val deserializersByFullTypeName: Map<String, () -> Deserializer<*>> by lazy {
        ClassGraph()
            .enableClassInfo()
            .acceptPackages(
                "protokt.v1.buf.validate.conformance.*",
                "protokt.v1.google.protobuf"
            )
            .scan()
            .use { result ->
                val messageClassList = result.allClasses.filter { it.implementsInterface(Message::class.java) }
                val classes = messageClassList.mapTo(ArrayList(messageClassList.size)) { it.loadClass() }
                System.err.println("class count: ${classes.size}")
                classes
            }
            .let {
                it.associateTo(HashMap(it.size)) { messageClass ->
                    messageClass.annotations.filterIsInstance<GeneratedMessage>().single().fullTypeName to {
                        messageClass.kotlin.nestedClasses
                            .single { it.simpleName == Empty.Deserializer::class.simpleName }
                            .objectInstance as Deserializer<*>
                    }
                }
            }
    }

    fun parse(fullTypeName: String, bytes: ByteString) =
        deserializersByFullTypeName.getValue(fullTypeName)().deserialize(bytes.newInput())
}
