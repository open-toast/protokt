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

package protokt.v1.testing.android

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.BufferedSink
import okio.BufferedSource
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream

class DataStoreHashCodeTest {
    object MapMessageSerializer : Serializer<MapMessage> {
        override val defaultValue = MapMessage {}

        override suspend fun readFrom(input: InputStream) =
            MapMessage.deserialize(input)

        override suspend fun writeTo(t: MapMessage, output: OutputStream) =
            t.serialize(output)
    }

    private class OkioSerializerWrapper<T>(
        private val delegate: Serializer<T>
    ) : OkioSerializer<T> {
        override val defaultValue: T
            get() = delegate.defaultValue

        override suspend fun readFrom(source: BufferedSource) =
            delegate.readFrom(source.inputStream())

        override suspend fun writeTo(t: T, sink: BufferedSink) =
            delegate.writeTo(t, sink.outputStream())
    }

    @Test
    fun `second write to DataStore succeeds without hashCode mutation error`() =
        runTest {
            val fileSystem = FakeFileSystem()
            val dataStore = DataStoreFactory.create(
                OkioStorage(
                    fileSystem,
                    OkioSerializerWrapper(MapMessageSerializer),
                    producePath = { "/test-cache".toPath() }
                )
            )

            dataStore.updateData {
                MapMessage {
                    entries = mapOf("a" to TestMessage { foo = "1" })
                }
            }

            dataStore.updateData {
                MapMessage {
                    entries = mapOf("b" to TestMessage { foo = "2" })
                }
            }

            val result = dataStore.data.first()
            assertThat(result.entries).containsKey("b")
        }
}
