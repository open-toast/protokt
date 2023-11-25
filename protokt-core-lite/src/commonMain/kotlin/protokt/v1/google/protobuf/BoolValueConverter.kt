/*
 * Copyright (c) 2020 Toast, Inc.
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

import protokt.v1.Converter

object BoolValueConverter : Converter<BoolValue, Boolean> {
    override val wrapper = Boolean::class

    override val wrapped = BoolValue::class

    override fun wrap(unwrapped: BoolValue) =
        unwrapped.value

    override fun unwrap(wrapped: Boolean) =
        BoolValue { value = wrapped }
}
