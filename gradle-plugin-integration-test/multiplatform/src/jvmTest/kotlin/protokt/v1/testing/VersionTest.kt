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

package protokt.v1.testing

import protokt.v1.Message
import kotlin.test.Test
import kotlin.test.assertTrue

class VersionTest {
    @Test
    fun `runtime version should match project version`() {
        val version = System.getenv("version")
        val runtimeJarPath = Message::class.java.protectionDomain.codeSource.location.toString()

        assertTrue(runtimeJarPath.endsWith("protokt-runtime-jvm-$version.jar"))
    }
}
