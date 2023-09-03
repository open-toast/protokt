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

import com.google.protobuf.gradle.protobuf
import protokt.v1.gradle.protokt

plugins {
    id("protokt.multiplatform-conventions")
    kotlin("kapt")
}

localProtokt(false)
enablePublishing()
compatibleWithAndroid()
trackKotlinApiCompatibility()

spotless {
    kotlin {
        targetExclude(
            "src/jvmMain/kotlin/com/toasttab/protokt/compiler/plugin.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/any.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/api.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/descriptor.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/duration.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/empty.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/field_mask.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/source_context.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/struct.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/timestamp.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/type.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/wrappers.kt"
        )
    }
}

protokt {
    generate {
        lite()
    }
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":extensions:protokt-extensions-api"))
                api(project(":protokt-runtime"))
            }
        }

        val jvmMain by getting {}

        val jvmTest by getting {
            dependencies {
                runtimeOnly(libs.protobuf.java)
            }
        }
    }
}

dependencies {
    protobuf(libs.protobuf.java)
}
