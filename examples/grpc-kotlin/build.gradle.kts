/*
 * Copyright (c) 2021 Toast Inc.
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
    application
}

dependencies {
    implementation(project(":examples:protos"))
    implementation(project(":protokt-runtime-grpc"))
    implementation(libraries.grpcKotlin)
    implementation(libraries.grpcStub)

    runtimeOnly(libraries.grpcNetty)
}

tasks.register<JavaExec>("HelloWorldServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "io.grpc.examples.helloworld.HelloWorldServerKt"
}

tasks.register<JavaExec>("RouteGuideServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "io.grpc.examples.routeguide.RouteGuideServerKt"
}

tasks.register<JavaExec>("AnimalsServer") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "io.grpc.examples.animals.AnimalsServerKt"
}

val helloWorldServerStartScripts = tasks.register<CreateStartScripts>("helloWorldServerStartScripts") {
    mainClassName = "io.grpc.examples.helloworld.HelloWorldServerKt"
    applicationName = "hello-world-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val routeGuideServerStartScripts = tasks.register<CreateStartScripts>("routeGuideServerStartScripts") {
    mainClassName = "io.grpc.examples.routeguide.RouteGuideServerKt"
    applicationName = "route-guide-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

val animalsServerStartScripts = tasks.register<CreateStartScripts>("animalsServerStartScripts") {
    mainClassName = "io.grpc.examples.animals.AnimalsServerKt"
    applicationName = "animals-server"
    outputDir = tasks.named<CreateStartScripts>("startScripts").get().outputDir
    classpath = tasks.named<CreateStartScripts>("startScripts").get().classpath
}

tasks.named("startScripts") {
    dependsOn(helloWorldServerStartScripts)
    dependsOn(routeGuideServerStartScripts)
    dependsOn(animalsServerStartScripts)
}
