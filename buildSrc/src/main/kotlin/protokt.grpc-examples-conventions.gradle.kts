/*
 * Copyright (c) 2022 Toast, Inc.
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

plugins {
    id("protokt.jvm-conventions")
    application
}

dependencies {
    implementation(project(":protokt-runtime-grpc-lite"))
    implementation(libs.grpc.stub)

    runtimeOnly(libs.grpc.netty)

    testRuntimeOnly(libs.junit.vintage)
}

tasks.register<JavaExec>("HelloWorldServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.helloworld.HelloWorldServerKt")
}

tasks.register<JavaExec>("RouteGuideServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.io.grpc.examples.routeguide.RouteGuideServerKt")
}

tasks.register<JavaExec>("AnimalsServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.animals.AnimalsServerKt")
}

tasks.register<JavaExec>("HelloWorldClient") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.helloworld.HelloWorldClientKt")
}

tasks.register<JavaExec>("RouteGuideClient") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.io.grpc.examples.routeguide.RouteGuideClientKt")
}

tasks.register<JavaExec>("AnimalsClient") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("protokt.v1.animals.AnimalsClientKt")
}

val helloWorldServerStartScripts = tasks.register<CreateStartScripts>("helloWorldServerStartScripts") {
    mainClass.set("protokt.v1.helloworld.HelloWorldServerKt")
    applicationName = "hello-world-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val routeGuideServerStartScripts = tasks.register<CreateStartScripts>("routeGuideServerStartScripts") {
    mainClass.set("protokt.v1.io.grpc.examples.routeguide.RouteGuideServerKt")
    applicationName = "route-guide-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val animalsServerStartScripts = tasks.register<CreateStartScripts>("animalsServerStartScripts") {
    mainClass.set("protokt.v1.animals.AnimalsServerKt")
    applicationName = "animals-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val helloWorldClientStartScripts = tasks.register<CreateStartScripts>("helloWorldClientStartScripts") {
    mainClass.set("protokt.v1.helloworld.HelloWorldClientKt")
    applicationName = "hello-world-client"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val routeGuideClientStartScripts = tasks.register<CreateStartScripts>("routeGuideClientStartScripts") {
    mainClass.set("protokt.v1.io.grpc.examples.routeguide.RouteGuideClientKt")
    applicationName = "route-guide-client"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val animalsClientStartScripts = tasks.register<CreateStartScripts>("animalsClientStartScripts") {
    mainClass.set("protokt.v1.animals.AnimalsClientKt")
    applicationName = "route-guide-client"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

tasks.named("startScripts") {
    dependsOn(helloWorldServerStartScripts)
    dependsOn(routeGuideServerStartScripts)
    dependsOn(animalsServerStartScripts)

    dependsOn(helloWorldClientStartScripts)
    dependsOn(routeGuideClientStartScripts)
    dependsOn(animalsClientStartScripts)
}
