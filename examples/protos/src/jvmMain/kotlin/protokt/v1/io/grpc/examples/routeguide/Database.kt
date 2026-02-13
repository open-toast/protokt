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

package protokt.v1.io.grpc.examples.routeguide

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

actual object Database {
    actual fun features(): List<Feature> =
        javaClass.getResourceAsStream("route_guide_db.json").use {
            ObjectMapper()
                .registerModule(KotlinModule.Builder().build())
                .readValue<JsonFeatureDatabase>(it!!.reader())
        }.feature.map { f ->
            Feature {
                name = f.name
                location = f.location?.let { l ->
                    Point {
                        latitude = l.latitude
                        longitude = l.longitude
                    }
                }
            }
        }

    private data class JsonFeatureDatabase(
        val feature: List<JsonFeature>
    )

    private data class JsonFeature(
        val name: String = "",
        val location: JsonPoint? = null
    )

    private data class JsonPoint(
        val latitude: Int = 0,
        val longitude: Int = 0
    )
}
