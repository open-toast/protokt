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

package protokt.v1.codegen.util

import protokt.v1.Extension
import protokt.v1.ExtensionCodecs
import protokt.v1.Message

internal object ProtoktExtensions {
    val file = Extension<Message, protokt.v1.FileOptions>(1253u, ExtensionCodecs.message(protokt.v1.FileOptions))
    val class_ = Extension<Message, protokt.v1.MessageOptions>(1253u, ExtensionCodecs.message(protokt.v1.MessageOptions))
    val property = Extension<Message, protokt.v1.FieldOptions>(1253u, ExtensionCodecs.message(protokt.v1.FieldOptions))
    val oneof = Extension<Message, protokt.v1.OneofOptions>(1253u, ExtensionCodecs.message(protokt.v1.OneofOptions))
    val enum_ = Extension<Message, protokt.v1.EnumOptions>(1253u, ExtensionCodecs.message(protokt.v1.EnumOptions))
    val enumValue =
        Extension<Message, protokt.v1.EnumValueOptions>(1253u, ExtensionCodecs.message(protokt.v1.EnumValueOptions))
    val service =
        Extension<Message, protokt.v1.ServiceOptions>(1253u, ExtensionCodecs.message(protokt.v1.ServiceOptions))
    val method = Extension<Message, protokt.v1.MethodOptions>(1253u, ExtensionCodecs.message(protokt.v1.MethodOptions))
}
