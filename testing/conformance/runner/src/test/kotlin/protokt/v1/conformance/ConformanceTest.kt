/*
 * Copyright (c) 2019 Toast, Inc.
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

package protokt.v1.conformance

import com.google.common.collect.Lists
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import protokt.v1.conformance.ConformanceTest.Codec
import protokt.v1.conformance.ConformanceTest.CollectionFactory.PERSISTENT
import protokt.v1.conformance.ConformanceTest.Platform.JS_IR
import protokt.v1.conformance.ConformanceTest.Platform.JVM
import protokt.v1.conformance.ConformanceTest.SerializationMode
import protokt.v1.testing.projectRoot
import protokt.v1.testing.runCommand
import java.io.File
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText

class ConformanceTest {
    enum class Platform(val project: String) {
        JVM("jvm"),
        JS_IR("js-ir")
    }

    enum class CollectionFactory { DEFAULT, PERSISTENT }

    enum class Codec { DEFAULT, KOTLIN }

    enum class SerializationMode { STANDARD, STREAMING }

    data class ConformanceConfig(
        val platform: Platform,
        val collectionFactory: CollectionFactory,
        val codec: Codec,
        val serializationMode: SerializationMode
    ) {
        fun driver(): Path =
            when (platform) {
                JVM -> jvmConformanceDriver
                JS_IR -> jsConformanceDriver(platform.project)
            }

        fun env(): Map<String, String> =
            when (platform) {
                JVM -> {
                    val flags = buildList {
                        if (collectionFactory == PERSISTENT) {
                            add("-Dprotokt.collection.factory=protokt.v1.PersistentCollectionFactory")
                        }
                        if (codec == Codec.KOTLIN) {
                            add("-Dprotokt.codec=protokt.v1.ProtoktCodec")
                        }
                        if (serializationMode == SerializationMode.STREAMING) {
                            add("-Dprotokt.streaming=true")
                        }
                    }
                    if (flags.isNotEmpty()) mapOf("JAVA_OPTS" to flags.joinToString(" ")) else emptyMap()
                }
                JS_IR -> buildMap {
                    if (collectionFactory == PERSISTENT) {
                        put("PROTOKT_COLLECTION_FACTORY", "protokt.v1.PersistentCollectionFactory")
                    }
                    if (codec == Codec.KOTLIN) {
                        put("PROTOKT_CODEC", "protokt.v1.ProtoktCodec")
                    }
                    if (serializationMode == SerializationMode.STREAMING) {
                        put("PROTOKT_STREAMING", "true")
                    }
                }
            }
    }

    companion object {
        @JvmStatic
        fun configurations() =
            Lists.cartesianProduct(
                Platform.entries,
                CollectionFactory.entries,
                Codec.entries,
                SerializationMode.entries
            ).map {
                ConformanceConfig(
                    it[0] as Platform,
                    it[1] as CollectionFactory,
                    it[2] as Codec,
                    it[3] as SerializationMode
                )
            }.filter {
                // streaming is only supported on JVM (protobuf-java InputStream/OutputStream)
                it.serializationMode != SerializationMode.STREAMING || it.platform == JVM
            }
    }

    @BeforeEach
    fun deleteFailingTests() {
        failingTests.deleteIfExists()
    }

    @ParameterizedTest
    @MethodSource("configurations")
    fun `run conformance tests`(config: ConformanceConfig) {
        try {
            val output = command(config).runCommand(projectRoot.toPath(), config.env())
            println(output.stderr)

            val allStderr = output.stderr + jsStderrLog(config.platform.project)
            verifyCollectionType(allStderr, config)
            verifyCodec(allStderr, config)
            verifyStreaming(allStderr, config)

            assertThat(output.stderr).contains("CONFORMANCE SUITE PASSED")
            val matches = " (\\d+) unexpected failures".toRegex().findAll(output.stderr).toList()
            // the current implementation runs two conformance suites
            assertThat(matches).hasSize(2)
            matches.forEach { assertThat(it.groupValues[1].toInt()).isEqualTo(0) }
            assertThat(output.exitCode).isEqualTo(0)
        } catch (t: Throwable) {
            if (failingTests.exists()) {
                println("Failing tests:\n" + failingTests.readText())
            }
            val jsStderr = jsStderrLog(config.platform.project)
            if (jsStderr.isNotEmpty()) {
                println("JS driver stderr:\n$jsStderr")
            }
            throw t
        }

        println("Conformance tests passed")
    }
}

private val jvmConformanceDriver =
    Path.of(File(projectRoot.parentFile, "jvm").absolutePath, "build", "install", "protokt-conformance", "bin", "protokt-conformance")

private fun jsConformanceDriver(project: String) =
    Path.of(File(projectRoot.parentFile, project).absolutePath, "run.sh")

private fun jsStderrLog(project: String): String {
    val log = Path.of(File(projectRoot.parentFile, project).absolutePath, "build", "conformance-run")
    return if (log.exists()) log.readText() else ""
}

private val failingTests =
    Path.of(projectRoot.absolutePath, "failing_tests.txt")

private fun failureList(project: String) =
    "--failure_list ../$project/failure_list_kt.txt"

private fun command(config: ConformanceTest.ConformanceConfig) =
    "${System.getProperty("conformance-runner")} --maximum_edition 2023 --enforce_recommended ${failureList(config.platform.project)} ${config.driver()}"

private const val UNMODIFIABLE_COLLECTION_TYPE = "protokt.v1.UnmodifiableList"
private val PERSISTENT_COLLECTION_TYPE = persistentListOf<Any>()::class.qualifiedName!!

private fun verifyCollectionType(stderr: String, config: ConformanceTest.ConformanceConfig) {
    val collectionType = "protoktCollectionFactory=(.+)".toRegex().find(stderr)?.groupValues?.get(1)?.trim()
    val expected = if (config.collectionFactory == PERSISTENT) PERSISTENT_COLLECTION_TYPE else UNMODIFIABLE_COLLECTION_TYPE
    val platformExpected = if (config.platform.project == "jvm") expected else expected.substringAfterLast(".")
    assertThat(collectionType).isEqualTo(platformExpected)
}

private const val PROTOBUF_JAVA_READER = "protokt.v1.ProtobufJavaReader"
private const val PROTOBUF_JS_READER = "protokt.v1.ProtobufJsReader"
private const val PROTOKT_READER = "protokt.v1.ProtoktReader"

private fun verifyCodec(stderr: String, config: ConformanceTest.ConformanceConfig) {
    val codecName = "protoktCodec=(.+)".toRegex().find(stderr)?.groupValues?.get(1)?.trim()
    val expected =
        if (config.codec == Codec.KOTLIN) {
            PROTOKT_READER
        } else {
            if (config.platform.project == "jvm") PROTOBUF_JAVA_READER else PROTOBUF_JS_READER
        }
    val platformExpected = if (config.platform.project == "jvm") expected else expected.substringAfterLast(".")
    assertThat(codecName).isEqualTo(platformExpected)
}

private fun verifyStreaming(stderr: String, config: ConformanceTest.ConformanceConfig) {
    val streaming = "protoktStreaming=(.+)".toRegex().find(stderr)?.groupValues?.get(1)?.trim()
    val expected = (config.serializationMode == SerializationMode.STREAMING).toString()
    assertThat(streaming).isEqualTo(expected)
}
