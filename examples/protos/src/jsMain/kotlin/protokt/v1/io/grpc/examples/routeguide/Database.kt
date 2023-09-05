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

package protokt.v1.io.grpc.examples.routeguide

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

actual object Database {
    actual fun features() =
        Json.decodeFromString<FeatureDatabase>(featuresJson())
            .feature
            .map { f ->
                Feature {
                    name = f.name
                    location = f.location.let { l ->
                        Point {
                            latitude = l.latitude
                            longitude = l.longitude
                        }
                    }
                }
            }

    private fun featuresJson(): String =
        js(
            """
                JSON.stringify(
                    require('../../../../../examples/protos/src/commonMain/resources/protokt/v1/io/grpc/examples/routeguide/route_guide_db.json')
                )
            """
        ) as String

    @Serializable
    data class FeatureDatabase(
        val feature: List<JsonFeature>
    )

    @Serializable
    data class JsonFeature(
        val name: String,
        val location: JsonPoint
    )

    @Serializable
    data class JsonPoint(
        val latitude: Int,
        val longitude: Int
    )
}
