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
import java.util.zip.ZipFile

plugins {
    id("protokt.jvm-conventions")
    id("com.google.protobuf")
    alias(libs.plugins.buildConfig)
    application
}

val grpcKotlinGenerator = configurations.create("grpcKotlinGenerator")
configurations.compileOnly {
    extendsFrom(grpcKotlinGenerator)
}

dependencies {
    grpcKotlinGenerator(libs.grpc.kotlin.gen) {
        artifact {
            name = "protoc-gen-grpc-kotlin"
            classifier = "jdk8"
            type = "jar"
            extension = "jar"
        }
    }
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

val codegenJar = tasks.named<Jar>("jar") {
    from(
        provider {
            zipTree(grpcKotlinGenerator.singleFile)
                .matching { include("io/grpc/kotlin/generator/**") }
        }
    )
}

val verifyGrpcKotlinGeneratorPackaging by tasks.registering {
    dependsOn(codegenJar)
    val archiveFile = codegenJar.flatMap { it.archiveFile }
    inputs.file(archiveFile)

    doLast {
        val grpcKotlinClasses =
            ZipFile(archiveFile.get().asFile).use { archive ->
                archive.entries().asSequence()
                    .map { it.name }
                    .filter { it.startsWith("io/grpc/kotlin/") && it.endsWith(".class") }
                    .toList()
            }

        check("io/grpc/kotlin/generator/GeneratorRunner.class" in grpcKotlinClasses)
        check(grpcKotlinClasses.all { it.startsWith("io/grpc/kotlin/generator/") }) {
            "Codegen JAR contains grpc-kotlin runtime classes"
        }
    }
}

val installCodegenDistribution = tasks.named<Sync>("installDist")
val codegenDistributionLib = layout.buildDirectory.dir("install/$CODEGEN_NAME/lib")
val verifyCodegenDistribution by tasks.registering {
    dependsOn(installCodegenDistribution)
    inputs.dir(codegenDistributionLib)

    doLast {
        val jars = codegenDistributionLib.get().asFile.listFiles().orEmpty().filter { it.extension == "jar" }
        check(jars.none { it.name.startsWith("grpc-kotlin-shim-") })

        val ownersByClass = mutableMapOf<String, MutableList<String>>()
        jars.forEach { jar ->
            ZipFile(jar).use { archive ->
                archive.entries().asSequence()
                    .map { it.name }
                    .filter { it.endsWith(".class") && !it.endsWith("module-info.class") }
                    .forEach { ownersByClass.getOrPut(it, ::mutableListOf).add(jar.name) }
            }
        }

        val duplicateClasses = ownersByClass.filterValues { it.size > 1 }
        check(duplicateClasses.isEmpty()) {
            duplicateClasses.entries.joinToString(prefix = "Duplicate classes in codegen distribution:\n", separator = "\n") { (name, owners) ->
                "$name: ${owners.joinToString()}"
            }
        }
    }
}

tasks.named("check") {
    dependsOn(verifyGrpcKotlinGeneratorPackaging, verifyCodegenDistribution)
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
        val protoc = configurations.named("protobufToolsLocator_protoc").get().singleFile
        protoc.setExecutable(true)
        environment("PROTOC_PATH", protoc)
    }
}

val bootstrapDirPath = rootProject.file("protokt-bootstrap/src/main/kotlin").absolutePath

val downloadWellKnownProtos by tasks.registering(DownloadWellKnownProtos::class) {
    protobufVersion = libs.versions.protobuf.java
    outputDir = layout.buildDirectory.dir("well-known-protos")
}

val regenerateBootstrap by tasks.registering(RegenerateBootstrap::class) {
    description = "Regenerates checked-in bootstrap types from descriptor.proto, plugin.proto, and protokt.proto"
    group = "protokt"
    dependsOn("installDist", downloadWellKnownProtos)

    protoc = configurations.named("protobufToolsLocator_protoc").map { layout.projectDirectory.file(it.singleFile.absolutePath) }
    codegen = layout.buildDirectory.file("install/$CODEGEN_NAME/bin/$CODEGEN_NAME")
    extensionsProtoDir = rootProject.layout.projectDirectory.dir("extensions/protokt-extensions-lite/src/extensions-proto")
    wellKnownProtosDir = downloadWellKnownProtos.flatMap { it.outputDir }
    generatedDir = layout.buildDirectory.dir("regenerate-bootstrap")
    bootstrapDir = layout.projectDirectory.dir(bootstrapDirPath)
}

val verifyBootstrap by tasks.registering(VerifyBootstrap::class) {
    description = "Verifies checked-in bootstrap types match what the current codegen would generate"
    group = "protokt"
    dependsOn(regenerateBootstrap)

    generatedDir = regenerateBootstrap.flatMap { it.generatedDir }
    bootstrapDir = layout.projectDirectory.dir(bootstrapDirPath)
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
