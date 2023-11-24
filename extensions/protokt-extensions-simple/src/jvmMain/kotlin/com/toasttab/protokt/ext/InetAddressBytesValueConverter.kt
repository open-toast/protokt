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

@file:Suppress("DEPRECATION")

package com.toasttab.protokt.ext

import com.toasttab.protokt.BytesValue
import com.toasttab.protokt.rt.Bytes
import java.net.InetAddress

@Deprecated("for backwards compatibility only")
object InetAddressBytesValueConverter : Converter<InetAddress, BytesValue> {
    override val wrapper = InetAddress::class

    override val wrapped = BytesValue::class

    override fun wrap(unwrapped: BytesValue): InetAddress {
        require(unwrapped.value.isNotEmpty()) {
            "cannot unwrap absent InetAddress"
        }
        return InetAddress.getByAddress(unwrapped.value.bytes)
    }

    override fun unwrap(wrapped: InetAddress): BytesValue =
        BytesValue { Bytes(wrapped.address) }
}
