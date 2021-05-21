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

import com.toasttab.protokt.gradle.CODEGEN_NAME
import com.toasttab.protokt.gradle.EXTENSIONS
import com.toasttab.protokt.gradle.ProtoktExtension
import com.toasttab.protokt.gradle.configureProtokt
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

val Project.buildSrcClasses
    get() = "${rootProject.projectDir}/buildSrc/build/classes/kotlin/main"

fun Project.localProtokt() {
    configureProtokt(this) {
        if (name !in setOf("protokt-core", "protokt-core-lite")) {
            project.afterEvaluate {
                dependencies {
                    add(EXTENSIONS, project(":${resolveProtoktCoreDep()}"))
                }
            }
        }

        "$rootDir/protokt-codegen/build/install/$CODEGEN_NAME/bin/$CODEGEN_NAME"
    }

    afterEvaluate {
        tasks.named("generateProto") {
            dependsOn(":protokt-codegen:installDist")
        }
    }
}

fun Project.pureKotlin() {
    tasks.withType<JavaCompile> {
        enabled = false
    }
}

fun Project.resolveProtoktCoreDep() =
    if (the<ProtoktExtension>().lite) {
        "protokt-core-lite"
    } else {
        "protokt-core"
    }
