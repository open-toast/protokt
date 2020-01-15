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

import com.toasttab.protokt.shared.kotlin
import com.toasttab.protokt.shared.main
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

fun Project.enablePublishing(defaultJars: Boolean = true) {
    apply(plugin = "maven-publish")

    val remoteUrl = project.properties[
        if (version.toString().endsWith("-SNAPSHOT")) {
            "publish.remote.url.snapshots"
        } else {
            "publish.remote.url.releases"
        }
    ] as String?

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "integration"
                setUrl("${project.rootProject.buildDir}/repos/integration")
            }

            if (remoteUrl != null) {
                maven {
                    name = "remote"
                    setUrl(remoteUrl)
                    credentials {
                        username = (properties["publish.remote.user"] ?: properties["artifactory_user"]) as String
                        password =
                            (properties["publish.remote.password"] ?: properties["artifactory_password"]) as String
                    }
                }
            }
        }
    }

    if (defaultJars) {
        tasks.register<Jar>("sourcesJar") {
            dependsOn("classes")

            from(this@enablePublishing.the<JavaPluginConvention>().sourceSets.main.kotlin)
            archiveClassifier.set("sources")
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("sources") {
                    from(components.getByName("java"))
                    artifact(tasks.getByName("sourcesJar"))
                    artifactId = project.name
                    version = "$version"
                    groupId = "$group"
                }
            }
        }
    }

    tasks.register("publishToIntegrationRepository") {
        group = "publishing"
        dependsOn(tasks.withType<PublishToMavenRepository>().matching {
            it.repository == project.the<PublishingExtension>().repositories.getByName("integration")
        })
    }

    tasks.register("publishToRemote") {
        enabled = remoteUrl != null
        group = "publishing"

        if (enabled) {
            dependsOn(tasks.withType<PublishToMavenRepository>().matching {
                it.repository == project.the<PublishingExtension>().repositories.getByName("remote")
            })
        }
    }
}
