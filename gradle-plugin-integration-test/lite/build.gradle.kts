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

import com.toasttab.protokt.gradle.kotlin

protokt {
    lite = true
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")

    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("com.google.protobuf:protobuf-javalite:3.16.0")
    testImplementation("com.toasttab.protokt:protokt-util:$version")
}

sourceSets {
    main {
        proto {
            srcDir("../regular/src/main/proto")
        }
    }
    test {
        kotlin {
            srcDir("../regular/src/test/kotlin")
        }
    }
}
