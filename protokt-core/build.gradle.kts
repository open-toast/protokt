/*
 * Copyright (c) 2019. Toast Inc.
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

apply(plugin = "idea")

localProtokt()
pureKotlin()
enablePublishing()

val wktSrc = "${project.buildDir}/extracted-include-protos/main/google/protobuf"
val wktDest = "src/main/proto/google/protobuf"

tasks.register("createProtoDir") {
    dependsOn("extractIncludeProto")
    doFirst {
        file(wktDest).mkdirs()
    }
}

tasks.register<Copy>("moveWellKnownTypes") {
    dependsOn("createProtoDir")

    from(wktSrc)
    into(wktDest)
    include("*.proto")
}

tasks.register<Delete>("deleteWellKnownTypes") {
    dependsOn("moveWellKnownTypes")
    delete(wktSrc)
}

afterEvaluate {
    tasks.named("generateProto") {
        dependsOn("deleteWellKnownTypes")
    }
}

dependencies {
    compileOnly(libraries.protobuf)
    api(project(":protokt-runtime"))
    implementation(libraries.kotlinReflect)
}
