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

@file:JvmName("Messages")

package protokt.v1.json

import com.google.protobuf.util.JsonFormat
import com.google.protobuf.util.JsonFormat.Printer
import protokt.v1.Message
import protokt.v1.google.protobuf.RuntimeContext
import protokt.v1.google.protobuf.toDynamicMessage

@JvmOverloads
fun Message.toJson(
    runtimeContext: RuntimeContext,
    printer: Printer = JsonFormat.printer()
) =
    printer.print(toDynamicMessage(runtimeContext))
