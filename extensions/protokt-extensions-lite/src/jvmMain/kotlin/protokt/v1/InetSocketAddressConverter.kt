/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1

object InetSocketAddressConverter : AbstractConverter<InetSocketAddress, java.net.InetSocketAddress>() {
    override fun wrap(unwrapped: InetSocketAddress) =
        java.net.InetSocketAddress(
            InetAddressBytesConverter.wrap(unwrapped.address),
            unwrapped.port
        )

    override fun unwrap(wrapped: java.net.InetSocketAddress) =
        InetSocketAddress {
            address = InetAddressBytesConverter.unwrap(wrapped.address)
            port = wrapped.port
        }
}
