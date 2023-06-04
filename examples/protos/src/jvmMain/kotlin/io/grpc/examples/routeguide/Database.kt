/*
 * Copyright (c) 2021 Toast, Inc.
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

package io.grpc.examples.routeguide

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

actual object Database {
    actual fun features(): List<Feature> {
        return javaClass.getResourceAsStream("route_guide_db.json").use {
            ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .registerModule(
                    SimpleModule()
                        .addKeyDeserializer(
                            UInt::class.java,
                            object : KeyDeserializer() {
                                override fun deserializeKey(key: String, ctxt: DeserializationContext) =
                                    key.toUInt()
                            }
                        )
                )
                .readValue<FeatureDatabase>(it!!.reader())
        }.feature
    }
}
