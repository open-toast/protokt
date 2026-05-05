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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal expect fun loadRouteGuideJson(): String

object Database {
    fun features(): List<Feature> =
        Json.decodeFromString<JsonFeatureDatabase>(loadRouteGuideJson())
            .feature
            .map { f ->
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
}

@Serializable
internal data class JsonFeatureDatabase(
    val feature: List<JsonFeature>
)

@Serializable
internal data class JsonFeature(
    val name: String = "",
    val location: JsonPoint? = null
)

@Serializable
internal data class JsonPoint(
    val latitude: Int = 0,
    val longitude: Int = 0
)
