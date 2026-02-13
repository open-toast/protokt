/*
 * Copyright (c) 2020 Toast, Inc.
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

import com.toasttab.expediter.gradle.ExpediterPlugin
import com.toasttab.expediter.gradle.config.ExpediterExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

fun Project.compatibleWithAndroid(api: Int = 19) {
    apply<ExpediterPlugin>()

    configure<ExpediterExtension> {
        platform {
            android {
                sdk = api
            }
        }

        application {
            sourceSet("main")
            configuration("runtimeClasspath")
            configuration("_provided_")
        }

        ignore {
            // JVM-specific implementations
            callerStartsWith("com/google/protobuf/UnsafeUtil\$JvmMemoryAccessor")
            // assume the parts of Guava that we are using are ok
            callerStartsWith("com/google/common")
            // persistent collection builders are only loaded when the user opts in
            // via the protokt.collections.persistent system property and adds
            // kotlinx-collections-immutable to their runtime classpath
            callerStartsWith("protokt/v1/Persistent")

            file(rootProject.layout.projectDirectory.file("expediter/expediter.json"))
        }

        failOnIssues = true
    }

    configurations.create("_provided_")

    dependencies {
        add("_provided_", libs.protobuf.lite)
    }
}
