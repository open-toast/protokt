/*
 * Copyright (c) 2021 Toast, Inc.
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
    id("protokt.android-conventions")
}

android {
    namespace = "com.toasttab.protokt.testing.android"
    compileSdk = 31
    namespace = "com.toasttab.protokt.v1.testing.android"

    sourceSets["test"].java.srcDir("../android/src/test/java")
}

localProtokt()
pureKotlin()

dependencies {
    testRuntimeOnly(libs.protobuf.lite)
}
