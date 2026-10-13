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

package protokt.v1

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Collections.synchronizedList
import java.util.concurrent.CountDownLatch

class ProtoktRuntimeTest {
    @BeforeEach
    fun resetBeforeTest() {
        RuntimeConfiguration.resetForTest()
    }

    @AfterEach
    fun resetAfterTest() {
        RuntimeConfiguration.resetForTest()
    }

    @Test
    fun `configuration selects both implementations`() {
        ProtoktRuntime.configure(TestCodec, TestCollectionFactory)

        assertThat(codec).isSameInstanceAs(TestCodec)
        assertThat(collectionFactory).isSameInstanceAs(TestCollectionFactory)
    }

    @Test
    fun `optional runtime modules do not change defaults`() {
        assertThat(codec).isSameInstanceAs(ProtoktCodec)
        assertThat(collectionFactory).isSameInstanceAs(DefaultCollectionFactory)
    }

    @Test
    fun `codec-only configuration retains default collections`() {
        ProtoktRuntime.configure(TestCodec)

        assertThat(codec).isSameInstanceAs(TestCodec)
        assertThat(collectionFactory).isSameInstanceAs(DefaultCollectionFactory)
    }

    @Test
    fun `collection-only configuration retains default codec`() {
        ProtoktRuntime.configure(TestCollectionFactory)

        assertThat(codec).isSameInstanceAs(ProtoktCodec)
        assertThat(collectionFactory).isSameInstanceAs(TestCollectionFactory)
    }

    @Test
    fun `second configuration fails`() {
        ProtoktRuntime.configure(TestCodec)

        val failure = assertThrows<IllegalStateException> { ProtoktRuntime.configure(TestCodec) }

        assertThat(failure).hasMessageThat().isEqualTo("Protokt runtime is already configured")
    }

    @Test
    fun `configuration after serialization fails`() {
        TestMessage.serialize()

        val failure = assertThrows<IllegalStateException> { ProtoktRuntime.configure(TestCodec) }

        assertThat(failure).hasMessageThat().isEqualTo("Protokt runtime cannot be configured after first use")
    }

    @Test
    fun `configuration after deserialization fails`() {
        TestDeserializer.deserialize(byteArrayOf())

        val failure = assertThrows<IllegalStateException> { ProtoktRuntime.configure(TestCodec) }

        assertThat(failure).hasMessageThat().isEqualTo("Protokt runtime cannot be configured after first use")
    }

    @Test
    fun `configuration after configured runtime use fails as first use`() {
        ProtoktRuntime.configure(TestCodec)
        TestMessage.serialize()

        val failure = assertThrows<IllegalStateException> { ProtoktRuntime.configure(TestCodec) }

        assertThat(failure).hasMessageThat().isEqualTo("Protokt runtime cannot be configured after first use")
    }

    @Test
    fun `configuration after generated collection construction fails`() {
        Collections.listBuilder<Int>()

        val failure = assertThrows<IllegalStateException> { ProtoktRuntime.configure(TestCollectionFactory) }

        assertThat(failure).hasMessageThat().isEqualTo("Protokt runtime cannot be configured after first use")
    }

    @Test
    fun `only one concurrent configuration succeeds`() {
        val start = CountDownLatch(1)
        val outcomes = synchronizedList(mutableListOf<Boolean>())
        val threads =
            List(16) {
                Thread {
                    start.await()
                    outcomes += runCatching { ProtoktRuntime.configure(TestCodec) }.isSuccess
                }.apply { start() }
            }

        start.countDown()
        threads.forEach { it.join() }

        assertThat(outcomes.count { it }).isEqualTo(1)
        assertThat(codec).isSameInstanceAs(TestCodec)
    }
}

private object TestCodec : Codec by ProtoktCodec

private object TestCollectionFactory : CollectionFactory by DefaultCollectionFactory

private object TestDeserializer : AbstractDeserializer<TestMessage>() {
    override fun deserialize(reader: Reader) =
        TestMessage
}

private object TestMessage : AbstractMessage() {
    override val unknownFields = UnknownFieldSet.empty()

    override fun serializedSize() =
        0

    override fun serialize(writer: Writer) =
        Unit
}
