/*
 * Copyright (c) 2019 Toast Inc.
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

package com.toasttab.protokt.ext

import java.net.InetAddress

@Deprecated("for backwards compatibility only", level = DeprecationLevel.HIDDEN)
object InetAddressConverter {
    val wrapper = InetAddress::class

    val wrapped = ByteArray::class

    fun wrap(unwrapped: ByteArray): InetAddress {
        require(unwrapped.isNotEmpty()) {
            "cannot unwrap absent InetAddress"
        }
        return InetAddress.getByAddress(unwrapped)
    }

    fun unwrap(wrapped: InetAddress) =
        wrapped.address
}
