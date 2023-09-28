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

package com.toasttab.protokt.grpc

@Deprecated("for backwards compatibility only")
class SchemaDescriptor(
    private val className: String,
    private val fileDescriptorClassName: String
) {

    /**
     * This returns a com.toasttab.protokt.FileDescriptor, which isn't available in the
     * lite runtime.
     */
    @Suppress("UNCHECKED_CAST")
    @Deprecated(
        "You can only use this with the non-lite runtime. If you're using the non-lite runtime, replace this with" +
            "`fileDescriptor` found in " +
            "`protokt-runtime-grpc/src/main/kotlin/com/toasttab/protokt/grpc/SchemaDescriptorExtensions.kt`",
        ReplaceWith("com.toasttab.protokt.grpc.fileDescriptor")
    )
    val fileDescriptorUntyped: Any by lazy {
        val clazz =
            try {
                Class.forName(fileDescriptorClassName) as Class<Any>
            } catch (ex: ClassNotFoundException) {
                throw IllegalStateException(
                    "descriptor class `$fileDescriptorClassName` not found for `$className`; " +
                        "are the descriptor objects available?",
                    ex
                )
            }
        val obj = clazz.objectInstance
        val getDescriptor = clazz.methods.find { it.name == "getDescriptor" }
            ?: error("No getDescriptor method found on $clazz")
        getDescriptor.invoke(obj)
    }
}

@Suppress("UNCHECKED_CAST")
private val <T> Class<T>.objectInstance: T
    get() = getDeclaredField("INSTANCE").get(null) as T
