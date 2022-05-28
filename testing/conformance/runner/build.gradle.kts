/*
 * Copyright (c) 2022 Toast Inc.
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
}

dependencies {
    testImplementation(project(":testing:testing-util"))
}

tasks.named<Test>("test") {
    outputs.upToDateWhen { false }
    dependsOn(":testing:conformance:js:compileProductionExecutableKotlinJs")
    dependsOn(":testing:conformance:jvm:installDist")
}

// if runner is compiled with LEGACY, this puts the JS executable where run.sh exepcts it
tasks.register<Copy>("unzipJs") {
    from(zipTree(file("../js/build/libs/js.jar")))
    into(file("../js/build/compileSync/main/productionExecutable/kotlin"))
}

// TODO: get rid of this, it's not needed, helpful for debugging
tasks.register<Exec>("runjs") {
    dependsOn(":testing:conformance:js:compileProductionExecutableKotlinJs")
    environment("DYLD_LIBRARY_PATH", "bin/darwin/.libs")
    executable(file("bin/darwin/conformance-test-runner"))
    args(
        "--enforce_recommended",
        "--failure_list",
        "/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js/failure_list_kt.txt",
        "/Users/andrewparmet/toast/git-repos/protokt/testing/conformance/js/run.sh"
    )
}
