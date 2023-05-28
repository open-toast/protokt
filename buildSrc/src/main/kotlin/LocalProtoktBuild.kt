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

import com.google.protobuf.gradle.GenerateProtoTask
import com.toasttab.protokt.v1.gradle.CODEGEN_NAME
import com.toasttab.protokt.v1.gradle.configureProtokt
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

fun Project.localProtokt() {
    configureProtokt(this, null) {
        "$rootDir/protokt-codegen/build/install/$CODEGEN_NAME/bin/$CODEGEN_NAME"
    }

    afterEvaluate {
        tasks.withType<GenerateProtoTask> {
            dependsOn(":protokt-codegen:installDist")
        }
    }
}

fun Project.includeBuildSrc(vararg filePatterns: String) {
    the<SourceSetContainer>()["main"].java {
        srcDir(rootProject.file("buildSrc/src/main/kotlin"))
        srcDir(rootProject.file("buildSrc/build/generated/protobuf-version"))

        filePatterns.forEach { include(it) }
    }
}

fun Project.pureKotlin() {
    tasks.withType<JavaCompile> {
        enabled = false
    }
}
