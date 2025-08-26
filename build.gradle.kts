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

allprojects {
    group = "com.toasttab.protokt"
    lint()
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

if (isRelease()) {
    nexusPublishing {
        repositories {
            sonatype {
                username.set(Remote.username)
                password.set(Remote.password)
                nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            }
        }
    }
}
