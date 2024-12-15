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

import com.google.protobuf.gradle.proto
import protokt.v1.gradle.protokt

plugins {
    id("protokt.multiplatform-published-conventions")
}

localProtokt()
compatibleWithAndroid()

spotless {
    kotlin {
        targetExclude(
            "src/jvmMain/kotlin/com/toasttab/protokt/ext/inet_socket_address.kt",
            "src/jvmMain/kotlin/com/toasttab/protokt/ext/protokt.kt"
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
                api(project(":extensions:protokt-extensions-lite"))

                // todo: not sure why this is needed
                api(project(":protokt-core"))
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("../protokt-extensions-lite/src/main/proto")
            srcDir("../protokt-extensions-lite/src/extensions-proto")
        }
    }
}
