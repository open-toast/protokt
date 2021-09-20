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

import com.toasttab.protokt.gradle.protoktExtensions
import org.gradle.util.VersionNumber
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version System.getProperty("kotlin.version", "1.4.32")
    id("com.diffplug.spotless") version "5.15.0"
}

buildscript {
    repositories {
        maven(url = "$projectDir/../build/repos/integration")
        gradlePluginPortal()
        mavenCentral()
    }

    dependencies {
        classpath("com.toasttab.protokt:protokt-gradle-plugin:$version")
    }
}

group = "com.toasttab.protokt.integration"

apply(plugin = "com.toasttab.protokt")

spotless {
    kotlin {
        ktlint()
        targetExclude("**/generated-sources/**")
    }

    kotlinGradle {
        ktlint()
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = "1.8"

            apiVersion = System.getProperty("kotlin.version")?.let { v ->
                VersionNumber.parse(v).run { "$major.$minor" }
            } ?: "1.4"
        }
    }

    test {
        systemProperty("version", version.toString())
        useJUnitPlatform()
    }

    compileJava {
        enabled = false
    }
}

repositories {
    maven(url = "$projectDir/../build/repos/integration")
    mavenCentral()
}

dependencies {
    protoktExtensions("com.toasttab.protokt:protokt-extensions:$version")

    implementation(kotlin("stdlib"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("com.google.protobuf:protobuf-javalite:3.16.0")
    testImplementation("com.toasttab.protokt:protokt-util:$version")
}
