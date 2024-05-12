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

import io.github.classgraph.ClassGraph
import protokt.v1.Deserializer
import protokt.v1.GeneratedMessage
import protokt.v1.google.protobuf.Empty
import java.io.InputStream
import kotlin.reflect.full.findAnnotation

object DynamicConcreteMessageDeserializer {
    private val deserializersByFullTypeName: Map<String, Deserializer<*>> by lazy {
        ClassGraph()
            .enableAnnotationInfo()
            .acceptPackages(
                "protokt.v1.buf.validate.conformance.*",
                "protokt.v1.google.protobuf"
            )
            .scan()
            .getClassesWithAnnotation(GeneratedMessage::class.java)
            .asSequence()
            .map { it.loadClass().kotlin }
            .associate { messageClass ->
                messageClass.findAnnotation<GeneratedMessage>()!!.fullTypeName to
                    messageClass
                        .nestedClasses
                        .single { it.simpleName == Empty.Deserializer::class.simpleName }
                        .objectInstance as Deserializer<*>
            }
    }

    fun parse(fullTypeName: String, bytes: InputStream) =
        deserializersByFullTypeName.getValue(fullTypeName).deserialize(bytes)
}
