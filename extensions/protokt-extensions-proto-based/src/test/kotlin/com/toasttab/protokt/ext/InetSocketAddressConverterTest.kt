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

import com.google.common.truth.Truth.assertThat
import java.net.InetAddress
import java.net.InetSocketAddress
import org.junit.jupiter.api.Test

class InetSocketAddressConverterTest {
    @Test
    fun `conversion works`() {
        val addr = InetSocketAddress(InetAddress.getLocalHost(), 8080)

        assertThat(
            InetSocketAddressConverter.wrap(InetSocketAddressConverter.unwrap(addr))
        ).isEqualTo(addr)
    }
}
