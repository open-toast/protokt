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

import com.google.protobuf.gradle.protobuf
import protokt.v1.gradle.protokt

plugins {
    id("protokt.multiplatform-published-conventions")
}

localProtokt()
compatibleWithAndroid()

spotless {
    kotlin {
        targetExclude(
            "src/jvmMain/kotlin/com/toasttab/protokt/**/*.kt"
        )
    }
}

protokt {
    generate {
        types = false
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":extensions:protokt-extensions-api"))
                api(project(":protokt-core-lite"))
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=protokt.v1.OnlyForUseByGeneratedProtoCode")
    }
}

dependencies {
    protobuf(libs.protobuf.java)
    protobuf(files("../protokt-runtime/src/main/resources"))
}
