/*
 * Copyright (c) 2019 Toast Inc.
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

import com.toasttab.protokt.gradle.protokt
import com.toasttab.protokt.gradle.protoktExtensions

plugins {
    id("protokt.jvm-conventions")
}

localProtokt()
pureKotlin()

protokt {
    generateGrpc = true
}

dependencies {
    protoktExtensions(project(":testing:options-api"))
    protoktExtensions(project(":third-party:proto-google-common-protos-extensions"))

    implementation(libraries.grpcStub)

    testImplementation(kotlin("reflect"))
    testImplementation(project(":testing:runtime-tests"))
    testImplementation(libraries.protobufJava)
}
