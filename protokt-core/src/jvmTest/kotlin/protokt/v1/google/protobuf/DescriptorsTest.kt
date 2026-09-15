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

package protokt.v1.google.protobuf

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DescriptorsTest {
    @Test
    fun enumFullNamesIncludeTheirContainingMessages() {
        val descriptor =
            fileDescriptor(
                FileDescriptorProto {
                    `package` = "example"
                    enumType = listOf(EnumDescriptorProto { name = "TopLevel" })
                    messageType =
                        listOf(
                            DescriptorProto {
                                name = "Outer"
                                enumType = listOf(EnumDescriptorProto { name = "OuterEnum" })
                                nestedType =
                                    listOf(
                                        DescriptorProto {
                                            name = "Inner"
                                            enumType = listOf(EnumDescriptorProto { name = "InnerEnum" })
                                        }
                                    )
                            }
                        )
                }
            )

        assertThat(descriptor.enumTypes.single().fullName).isEqualTo("example.TopLevel")
        assertThat(descriptor.messageTypes.single().enumTypes.single().fullName).isEqualTo("example.Outer.OuterEnum")
        assertThat(descriptor.messageTypes.single().nestedTypes.single().enumTypes.single().fullName)
            .isEqualTo("example.Outer.Inner.InnerEnum")
    }

    @Test
    fun nestedEnumFullNameWithoutPackageStartsWithContainingMessage() {
        val descriptor =
            fileDescriptor(
                FileDescriptorProto {
                    messageType =
                        listOf(
                            DescriptorProto {
                                name = "Container"
                                enumType = listOf(EnumDescriptorProto { name = "State" })
                            }
                        )
                }
            )

        assertThat(descriptor.messageTypes.single().enumTypes.single().fullName).isEqualTo("Container.State")
    }
}

private fun fileDescriptor(proto: FileDescriptorProto): FileDescriptor {
    val data = proto.serialize().map { (it.toInt() and 0xff).toChar() }.toCharArray().concatToString()
    return FileDescriptor.buildFrom(arrayOf(data), emptyList())
}
