/*
 * Copyright (c) 2026 Toast, Inc.
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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package protokt.v1.benchmarks

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.rewind

actual fun readDatasetBytes(name: String): ByteArray {
    val file = fopen("../build/datasets/dataset-$name", "rb") ?: error("Cannot open dataset: $name")
    fseek(file, 0, SEEK_END)
    val size = ftell(file).toInt()
    rewind(file)
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        fread(pinned.addressOf(0), 1u.convert(), size.convert(), file)
    }
    fclose(file)
    return bytes
}
