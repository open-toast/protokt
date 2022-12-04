/*
 * Copyright (c) 2022 Toast Inc.
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

package com.toasttab.protokt.codegen.util

object ErrorContext {
    private const val FILE_NAME = "fileName"
    private const val MESSAGE_NAME = "messageName"
    private const val ENUM_NAME = "enumName"
    private const val SERVICE_NAME = "serviceName"

    private val context = mutableMapOf<String, Any?>()

    fun fileName() =
        context[FILE_NAME]

    fun messageName() =
        context[MESSAGE_NAME]

    fun <T> withFileName(name: String, action: () -> T) =
        withProperty(FILE_NAME, name, action)

    fun <T> withMessageName(name: Any, action: () -> T) =
        withProperty(MESSAGE_NAME, name, action)

    fun <T> withEnumName(name: Any, action: () -> T) =
        withProperty(ENUM_NAME, name, action)

    fun <T> withServiceName(name: Any, action: () -> T) =
        withProperty(SERVICE_NAME, name, action)

    private fun <T> withProperty(propertyName: String, propertyValue: Any, action: () -> T): T {
        context[propertyName] = propertyValue
        val result = action()
        context[propertyName] = null
        return result
    }
}
