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

package com.toasttab.protokt.options

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.truth.Truth.assertThat
import com.toasttab.model.AnEnum
import com.toasttab.model.RootMessage
import com.toasttab.model.ToDeserialize
import com.toasttab.protokt.StringValue
import com.toasttab.protokt.rt.KtEnum
import com.toasttab.protokt.rt.KtEnumDeserializer
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.Unknown
import org.junit.jupiter.api.Test

class JsonDeserializationTest {
    private val mapper =
        ObjectMapper()
            .registerModule(
                SimpleModule()
                    .addDeserializer(
                        RootMessage.OneofField::class.java,
                        OneofFieldDeserializer
                    )
                    .addDeserializer(
                        AnEnum::class.java,
                        EnumFieldDeserializer(AnEnum)
                    )
            )
            .registerModule(KotlinModule())
            .addMixIn(KtMessage::class.java, KtMixin::class.java)

    // Cannot simple annotate messageSize with @delegate:Transient, as lazy properties' getters
    // don't properly inherit the annotation. For now this seems to be the only way to always
    // exclude messageSize from being serialized.
    private abstract class KtMixin(
        @Suppress("UNUSED")
        @get:JsonIgnore
        val messageSize: Int,

        @Suppress("UNUSED")
        @get:JsonIgnore
        val unknown: Map<Int, Unknown>
    )

    @Test
    fun `protokt class can be deserialized from JSON`() {
        val original =
            ToDeserialize(
                StringValue("some-string"),
                RootMessage(
                    "some-field",
                    RootMessage.OneofField.Name("asdf")
                ),
                AnEnum.FIRST
            )

        val string = mapper.writeValueAsString(original)

        val deserialized = mapper.readValue(string, ToDeserialize::class.java)

        assertThat(deserialized).isEqualTo(original)
    }

    // Jackson does not support deserializing to sealed classes out of the box
    object OneofFieldDeserializer : JsonDeserializer<RootMessage.OneofField>() {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext): RootMessage.OneofField {
            val map = ctx.readValue(p, Map::class.java)
            return when (val n = map.keys.first()) {
                "name" -> RootMessage.OneofField.Name(map.values.first() as String)
                else -> throw IllegalArgumentException("Unknown oneof type: $n")
            }
        }
    }

    // Jackson does not support deserializing to sealed classes out of the box
    class EnumFieldDeserializer<T : KtEnum>(
        private val deserializer: KtEnumDeserializer<T>
    ) : JsonDeserializer<T>() {
        override fun deserialize(p: JsonParser, ctx: DeserializationContext) =
            deserializer.from(ctx.readValue(p, Map::class.java)["value"] as Int)
    }
}
