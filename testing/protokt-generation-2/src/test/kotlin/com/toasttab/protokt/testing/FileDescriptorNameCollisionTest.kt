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

package com.toasttab.protokt.testing

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import toasttab.protokt.testing.file_descriptor_name_collision_file_descriptor

class FileDescriptorNameCollisionTest {
    @Test
    fun `files with same name have same descriptor object name`() {
        assertThat(toasttab.protokt.testing.rt.file_descriptor_name_collision1_file_descriptor::class.simpleName)
            .isEqualTo(toasttab.protokt.testing.other.file_descriptor_name_collision1_file_descriptor::class.simpleName)
    }

    @Test
    fun `file in other project depending on a duplicated name chooses the right one`() {
        // having only one dependency ensures that any deduplication done on the descriptor object names is resilient
        assertThat(file_descriptor_name_collision_file_descriptor.descriptor.dependencies)
            .containsExactly(toasttab.protokt.testing.other.file_descriptor_name_collision1_file_descriptor.descriptor)
    }
}
