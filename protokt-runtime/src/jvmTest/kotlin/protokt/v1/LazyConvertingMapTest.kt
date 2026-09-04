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
import org.junit.jupiter.api.Test

@OptIn(OnlyForUseByGeneratedProtoCode::class)
class LazyConvertingMapTest {
    @Test
    fun `wrapped keys remain in wire form`() {
        val converter = CountingKeyConverter()
        val map = LazyConvertingMap<ValueKey, String>(mapOf(WireKey("key") to "value"), true, false, converter, null)

        val initialHashCode = map.hashCode()
        assertThat(converter.wrapCount).isEqualTo(1)
        converter.resetCounts()

        assertThat(map[ValueKey("key")]).isEqualTo("value")
        assertThat(map.containsKey(ValueKey("key"))).isTrue()
        assertThat(converter.unwrapCount).isEqualTo(2)
        assertThat(converter.wrapCount).isEqualTo(0)

        val wireEntries = mutableListOf<Pair<WireKey, String>>()
        map.wireEntryForEach<WireKey, String> { key, value -> wireEntries += key to value }
        assertThat(wireEntries).containsExactly(WireKey("key") to "value")
        assertThat(converter.wrapCount).isEqualTo(0)

        assertThat(map.entries.single().key).isEqualTo(ValueKey("key"))
        assertThat(converter.wrapCount).isEqualTo(1)
        assertThat(map.hashCode()).isEqualTo(initialHashCode)
    }

    @Test
    fun `plus stores new wrapped keys in wire form`() {
        val converter = CountingKeyConverter()
        val map = LazyConvertingMap<ValueKey, String>(mapOf(WireKey("first") to "one"), true, false, converter, null)

        val extended = map + (ValueKey("second") to "two")

        assertThat(extended[ValueKey("first")]).isEqualTo("one")
        assertThat(extended[ValueKey("second")]).isEqualTo("two")
        assertThat(converter.wrapCount).isEqualTo(0)
        assertThat(converter.unwrapCount).isEqualTo(3)

        val wireEntries = mutableListOf<Pair<WireKey, String>>()
        extended.wireEntryForEach<WireKey, String> { key, value -> wireEntries += key to value }
        assertThat(wireEntries).containsExactly(WireKey("first") to "one", WireKey("second") to "two").inOrder()
        assertThat(converter.wrapCount).isEqualTo(0)
    }
}

private data class WireKey(val value: String)

private data class ValueKey(val value: String)

private class CountingKeyConverter : Converter<WireKey, ValueKey> {
    override val wireType = WireKey::class
    override val valueType = ValueKey::class

    var wrapCount = 0
        private set

    var unwrapCount = 0
        private set

    fun resetCounts() {
        wrapCount = 0
        unwrapCount = 0
    }

    override fun wrap(unwrapped: WireKey): ValueKey {
        wrapCount++
        return ValueKey(unwrapped.value)
    }

    override fun unwrap(wrapped: ValueKey): WireKey {
        unwrapCount++
        return WireKey(wrapped.value)
    }
}
