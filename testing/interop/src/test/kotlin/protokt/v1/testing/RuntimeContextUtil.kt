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

package protokt.v1.testing

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import io.github.classgraph.ClassGraph
import protokt.v1.GeneratedFileDescriptor
import protokt.v1.google.protobuf.FileDescriptor
import protokt.v1.google.protobuf.RuntimeContext
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

fun getContextReflectively() =
    RuntimeContext(getDescriptors())

private fun getDescriptors() =
    ClassGraph()
        .enableAnnotationInfo()
        .scan()
        .use {
            it.getClassesWithAnnotation(GeneratedFileDescriptor::class.java)
                .map { info ->
                    @Suppress("UNCHECKED_CAST")
                    info.loadClass().kotlin as KClass<Any>
                }
        }
        .asSequence()
        .map { klassWithDescriptor ->
            klassWithDescriptor
                .declaredMemberProperties
                .single { it.returnType.classifier == FileDescriptor::class }
                .get(klassWithDescriptor.objectInstance!!) as FileDescriptor
        }
        .flatMap { it.toProtobufJavaDescriptor().messageTypes }
        .flatMap(::collectDescriptors)
        .asIterable()

private fun collectDescriptors(descriptor: Descriptors.Descriptor): Iterable<Descriptors.Descriptor> =
    listOf(descriptor) + descriptor.nestedTypes.flatMap(::collectDescriptors)

private fun FileDescriptor.toProtobufJavaDescriptor(): Descriptors.FileDescriptor =
    Descriptors.FileDescriptor.buildFrom(
        DescriptorProtos.FileDescriptorProto.parseFrom(proto.serialize()),
        dependencies.map { it.toProtobufJavaDescriptor() }.toTypedArray(),
        true,
    )
