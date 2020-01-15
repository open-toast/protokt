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

enablePublishing(defaultJars = false)

dependencies {
    api(project(":extensions:protokt-extensions-simple"))
    api(project(":extensions:protokt-extensions-proto-based"))
    api(project(":extensions:protokt-extensions-wrappers"))
    api(project(":extensions:protokt-extensions-api"))
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("extensionsJar") {
            from(project.components["java"])
            artifactId = "protokt-extensions"
            version = "${rootProject.version}"
            groupId = "${rootProject.group}"
        }
    }
}
