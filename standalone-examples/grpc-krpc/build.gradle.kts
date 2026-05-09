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

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val protoktVersion =
    file("../../build/repos/integration/com/toasttab/protokt/v1/protokt-gradle-plugin")
        .listFiles { f -> f.isDirectory }!!
        .single()
        .name

buildscript {
    val protoktVersion =
        file("../../build/repos/integration/com/toasttab/protokt/v1/protokt-gradle-plugin")
            .listFiles { f -> f.isDirectory }!!
            .single()
            .name

    repositories {
        maven(url = "../../build/repos/integration")
        maven("https://packages.jetbrains.team/maven/p/krpc/grpc")
        mavenCentral()
        gradlePluginPortal()
    }

    configurations.all {
        resolutionStrategy.force("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.krpcKotlin.get()}")
    }

    dependencies {
        classpath("com.toasttab.protokt.v1:protokt-gradle-plugin:$protoktVersion")
        classpath("${libs.kotlinGradlePlugin.get().module}:${libs.versions.krpcKotlin.get()}")
        classpath(libs.kotlinx.rpc.gradlePlugin)
        classpath(libs.protobuf.gradlePlugin)
    }
}

apply(plugin = "org.jetbrains.kotlin.multiplatform")
apply(plugin = "com.toasttab.protokt.v1")
apply(plugin = "org.jetbrains.kotlinx.rpc.plugin")

repositories {
    maven(url = "../../build/repos/integration")
    maven("https://packages.jetbrains.team/maven/p/krpc/grpc")
    mavenCentral()
}

extensions.configure<protokt.v1.gradle.ProtoktExtension>("protokt") {
    generate {
        grpcKrpcLite()
    }
}

dependencies {
    "protobuf"(files("../../examples/protos/src/main/proto/helloworld"))
}

configure<KotlinMultiplatformExtension> {
    jvm {
        compilerOptions {
            jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        }
    }

    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable()
        }
    }

    compilerOptions {
        allWarningsAsErrors = true
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-opt-in=kotlinx.rpc.internal.utils.ExperimentalRpcApi",
        )
    }

    sourceSets.getByName("commonMain") {
        dependencies {
            implementation("com.toasttab.protokt.v1:protokt-runtime-grpc-krpc:$protoktVersion")
            implementation(libs.kotlinx.rpc.grpc.client)
            implementation(libs.kotlinx.rpc.grpc.server)
            implementation(libs.kotlinx.coroutines.core)
        }
    }

    sourceSets.getByName("commonTest") {
        dependencies {
            implementation(kotlin("test"))
        }
    }

    sourceSets.getByName("jvmMain") {
        dependencies {
            implementation(libs.grpc.netty)
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get().toInt()))
    }
}
