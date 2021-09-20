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

package com.toasttab.protokt.testing

import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.util.getProtoktVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VersionTest {
    @Test
    fun `runtime version should match project version`() {
        val version = System.getProperty("version")
        val runtimeVersion = getProtoktVersion(KtMessage::class)

        assertEquals(version, runtimeVersion)
    }
}
