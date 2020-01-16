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

import com.toasttab.protokt.shared.MANIFEST_VERSION_PROPERTY
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libraries.kotlinPlugin)
        classpath(libraries.protobufPlugin)
    }
}

allprojects {
    lint()
    group = "com.toasttab.protokt"
}

subprojects {
    repositories {
        jcenter()
        maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    }

    apply(plugin = "idea")
    apply(plugin = "kotlin")

    dependencies {
        add("api", libraries.kotlinStdlib)

        add("testImplementation", libraries.kotlinTest)
        add("testImplementation", libraries.junit)
        add("testImplementation", libraries.truth)
    }

    version = rootProject.version

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xinline-classes")
        }
    }

    tasks.withType<Jar> {
        manifest {
            attributes(
                MANIFEST_VERSION_PROPERTY to "${project.version}"
            )
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
