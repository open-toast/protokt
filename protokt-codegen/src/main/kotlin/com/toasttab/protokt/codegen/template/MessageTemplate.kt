/*
 * Copyright (c) 2020 Toast Inc.
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

package com.toasttab.protokt.codegen.template

object MessageTemplate : StTemplate<MessageVariable>(
    MessageGroup,
    "message",
    setOf(
        MessageVariable.Message,
        MessageVariable.Entry,
        MessageVariable.Serialize,
        MessageVariable.Deserialize,
        MessageVariable.Sizeof,
        MessageVariable.Inner,
        MessageVariable.Params,
        MessageVariable.Oneofs,
        MessageVariable.Options
    )
)

sealed class MessageVariable(
    override val name: String
) : TemplateVariable {
    object Message : MessageVariable("message")
    object Entry : MessageVariable("entry")
    object Serialize : MessageVariable("serialize")
    object Deserialize : MessageVariable("deserialize")
    object Sizeof : MessageVariable("sizeof")
    object Inner : MessageVariable("inner")
    object Params : MessageVariable("params")
    object Oneofs : MessageVariable("oneofs")
    object Options : MessageVariable("options")
}
