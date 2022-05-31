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

import com.google.protobuf.gradle.protobuf
import com.toasttab.protokt.gradle.protokt
import com.toasttab.protokt.gradle.protoktExtensions

plugins {
    id("protokt.jvm-conventions")
    kotlin("kapt")
}

localProtokt()
pureKotlin()
enablePublishing()
trackKotlinApiCompatibility()

protokt {
    lite = true
}

dependencies {
    protoktExtensions(project(":extensions:protokt-extensions-jvm-simple"))

    // TODO: Why doesn't this work? Why must this be a `protobuf` dependency?
    // protoktExtensions(project(":extensions:protokt-extensions-lite"))
    protobuf(files("../protokt-extensions-lite/src/main/proto"))

    implementation(project(":extensions:protokt-extensions-api"))
    implementation(libraries.autoServiceAnnotations)

    kapt(libraries.autoService)
}
