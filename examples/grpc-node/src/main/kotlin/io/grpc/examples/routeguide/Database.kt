package io.grpc.examples.routeguide

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object Database {
    fun features() =
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
                    require('../../../../../examples/protos/src/main/resources/io/grpc/examples/routeguide/route_guide_db.json')
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
