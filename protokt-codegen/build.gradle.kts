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

import protokt.v1.gradle.CODEGEN_NAME

plugins {
    id("protokt.jvm-conventions")
    id("com.google.protobuf")
    alias(libs.plugins.buildConfig)
    application
}

defaultProtoc()

enablePublishing(defaultJars = false)

application {
    applicationName = CODEGEN_NAME
    mainClass.set("protokt.v1.codegen.MainKt")
}

tasks.named<CreateStartScripts>("startScripts") {
    doLast {
        // Replace the long enumerated classpath with a wildcard to avoid
        // exceeding Windows' command line length limit
        windowsScript.writeText(
            windowsScript.readText().replace(
                Regex("set CLASSPATH=.*"),
                "set CLASSPATH=%APP_HOME%\\\\lib\\\\*"
            )
        )
        unixScript.writeText(
            unixScript.readText().replace(
                Regex("CLASSPATH=.*"),
                "CLASSPATH=\\\$APP_HOME/lib/*"
            )
        )
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
    }
}

dependencies {
    implementation(project(":protokt-bootstrap"))
    implementation(project(":protokt-runtime-protobuf-java"))
    implementation(project(":protokt-runtime-grpc-lite"))
    implementation(project(":grpc-kotlin-shim"))

    implementation(kotlin("reflect"))

    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.stub)
    implementation(libs.kotlinLogging)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktlint)
    implementation(libs.ktlintRuleSetStandard)
    implementation(libs.protobuf.java)
    implementation(libs.slf4jSimple)

    testImplementation(project(":testing:testing-util"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.truth)
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("dist") {
            artifact(
                mapOf(
                    "source" to tasks.findByName("distZip"),
                    "extension" to "zip",
                    "classifier" to "dist"
                )
            )
            artifactId = project.name
            version = "${rootProject.version}"
            groupId = "${rootProject.group}"
        }
    }
}

tasks.withType<Test> {
    afterEvaluate {
        environment("PROTOC_PATH", configurations.named("protobufToolsLocator_protoc").get().singleFile)
    }
}

val bootstrapDir = rootProject.file("protokt-bootstrap/src/main/kotlin")

val downloadWellKnownProtos by tasks.registering {
    val outputDir = layout.buildDirectory.dir("well-known-protos")
    val version = libs.versions.protobuf.java.get()
    outputs.dir(outputDir)

    doLast {
        val baseUrl = "https://raw.githubusercontent.com/protocolbuffers/protobuf/v$version/src"
        val protos = listOf(
            "google/protobuf/descriptor.proto",
            "google/protobuf/compiler/plugin.proto"
        )
        protos.forEach { proto ->
            val target = outputDir.get().file(proto).asFile
            target.parentFile.mkdirs()
            uri("$baseUrl/$proto").toURL().openStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }
}

val regenerateBootstrap by tasks.registering(Exec::class) {
    description = "Regenerates checked-in bootstrap types from descriptor.proto, plugin.proto, and protokt.proto"
    group = "protokt"
    dependsOn("installDist", downloadWellKnownProtos)

    val protoc = configurations.named("protobufToolsLocator_protoc").map { it.singleFile }
    val codegen = layout.buildDirectory.file("install/$CODEGEN_NAME/bin/$CODEGEN_NAME")
    val extensionsProtoDir = rootProject.file("extensions/protokt-extensions-lite/src/extensions-proto")
    val wellKnownProtosDir = layout.buildDirectory.dir("well-known-protos")
    val outputDir = temporaryDir

    inputs.file(codegen)
    inputs.dir(extensionsProtoDir)
    outputs.dir(bootstrapDir)

    doFirst {
        outputDir.deleteRecursively()
        outputDir.mkdirs()
    }

    commandLine(
        protoc.get().absolutePath,
        "--plugin=protoc-gen-custom=${codegen.get().asFile.absolutePath}",
        "--custom_out=$outputDir",
        "--custom_opt=kotlin_target=jvm,generate_types=true,generate_descriptors=false",
        "--proto_path=${wellKnownProtosDir.get().asFile.absolutePath}",
        "--proto_path=$extensionsProtoDir",
        "google/protobuf/descriptor.proto",
        "google/protobuf/compiler/plugin.proto",
        "protokt/v1/protokt.proto"
    )

    doLast {
        copy {
            from("$outputDir/protokt/v1/google/protobuf/descriptor.kt")
            into("$bootstrapDir/protokt/v1/google/protobuf")
            rename { "Descriptor.kt" }
        }
        copy {
            from("$outputDir/protokt/v1/google/protobuf/compiler/plugin.kt")
            into("$bootstrapDir/protokt/v1/google/protobuf/compiler")
            rename { "Plugin.kt" }
        }
        copy {
            from("$outputDir/protokt/v1/protokt.kt")
            into("$bootstrapDir/protokt/v1")
            rename { "Protokt.kt" }
        }
    }
}

val verifyBootstrap by tasks.registering {
    description = "Verifies checked-in bootstrap types match what the current codegen would generate"
    group = "protokt"
    dependsOn(regenerateBootstrap)

    doLast {
        val tempDir = regenerateBootstrap.get().temporaryDir
        val pairs = listOf(
            file("$tempDir/protokt/v1/google/protobuf/descriptor.kt") to file("$bootstrapDir/protokt/v1/google/protobuf/Descriptor.kt"),
            file("$tempDir/protokt/v1/google/protobuf/compiler/plugin.kt") to file("$bootstrapDir/protokt/v1/google/protobuf/compiler/Plugin.kt"),
            file("$tempDir/protokt/v1/protokt.kt") to file("$bootstrapDir/protokt/v1/Protokt.kt")
        )
        pairs.forEach { (generated, checkedIn) ->
            check(generated.readText() == checkedIn.readText()) {
                "Bootstrap file ${checkedIn.name} is out of date. Run: ./gradlew :protokt-codegen:regenerateBootstrap"
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("../shared-src/codegen")
            srcDir("../shared-src/reflect")
        }
    }
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("protokt.v1.gradle")
    buildConfigField("String", "DEFAULT_PROTOBUF_VERSION", "\"${libs.versions.protobuf.java.get()}\"")
    buildConfigField("String", "PROTOKT_VERSION", "\"$version\"")
}
