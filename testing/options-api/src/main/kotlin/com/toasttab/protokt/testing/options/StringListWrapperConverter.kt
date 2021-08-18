/*
 * Copyright (c) 2021 Toast Inc.
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

package com.toasttab.protokt.testing.options

import com.google.auto.service.AutoService
import com.toasttab.protokt.ext.Converter
import kotlin.reflect.KClass

@AutoService(Converter::class)
object StringListWrapperConverter : Converter<List<String>, StringListWrapper> {
    override val wrapped = StringListWrapper::class

    @Suppress("UNCHECKED_CAST")
    override val wrapper = List::class as KClass<List<String>>

    override fun unwrap(wrapped: List<String>) =
        StringListWrapper { value = wrapped }

    override fun wrap(unwrapped: StringListWrapper) =
        unwrapped.value
}
