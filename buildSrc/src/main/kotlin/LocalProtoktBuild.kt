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

import com.toasttab.protokt.shared.configureProtokt
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType

fun Project.localProtokt() {
    configureProtokt(project) { _ ->
        "${project.rootDir}/protokt-codegen/build/install/protoc-gen-protokt/bin/protoc-gen-protokt"
    }

    project.afterEvaluate {
        project.tasks.named("generateProto") {
            dependsOn(":protokt-codegen:installDist")
        }
    }
}

fun Project.pureKotlin() {
    tasks.withType<JavaCompile> {
        enabled = false
    }
}
