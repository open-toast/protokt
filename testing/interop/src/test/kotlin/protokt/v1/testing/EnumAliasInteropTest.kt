/*
 * Copyright (c) 2024 Toast, Inc.
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

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import protokt.v1.testing.aliases.AliasMessage
import protokt.v1.testing.aliases.Choice
import protokt.v1.testing.aliases.ObjectFirstChoice
import protokt.v1.testing.aliases.java.EnumAliases.AliasMessage as JavaMessage
import protokt.v1.testing.aliases.java.EnumAliases.Choice as JavaChoice

class EnumAliasInteropTest {
    @Test
    fun `accessing the first constant before its alias preserves identity`() {
        val first = ObjectFirstChoice.PRIMARY

        assertThat(ObjectFirstChoice.ALIAS).isSameInstanceAs(first)
    }

    @Test
    fun `alias constants share identity and name in both generators`() {
        val alias = Choice.ALIAS

        assertThat(alias).isSameInstanceAs(Choice.PRIMARY)
        assertThat(Choice.OTHER).isSameInstanceAs(Choice.OTHER_ALIAS)
        assertThat(JavaChoice.ALIAS).isSameInstanceAs(JavaChoice.PRIMARY)
        assertThat(alias.name).isEqualTo(JavaChoice.ALIAS.name)
        assertThat(Choice.deserialize(0)).isSameInstanceAs(alias)
    }

    @Test
    fun `messages using aliases match protobuf-java across serialization`() {
        val message =
            AliasMessage {
                choice = Choice.OTHER_ALIAS
                choices = listOf(Choice.ALIAS, Choice.OTHER_ALIAS)
                choicesByName = mapOf("key" to Choice.OTHER_ALIAS)
            }
        val javaMessage =
            JavaMessage.newBuilder()
                .setChoice(JavaChoice.OTHER_ALIAS)
                .addChoices(JavaChoice.ALIAS)
                .addChoices(JavaChoice.OTHER_ALIAS)
                .putChoicesByName("key", JavaChoice.OTHER_ALIAS)
                .build()

        assertThat(message.serialize()).isEqualTo(javaMessage.toByteArray())
        assertThat(AliasMessage.deserialize(javaMessage.toByteArray())).isEqualTo(message)
        assertThat(JavaMessage.parseFrom(message.serialize())).isEqualTo(javaMessage)
        assertThat(AliasMessage.deserialize(message.serialize()).hashCode()).isEqualTo(message.hashCode())
        assertThat(message.choice.name).isEqualTo(javaMessage.choice.name)
    }
}
