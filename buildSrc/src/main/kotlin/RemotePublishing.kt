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

import com.toasttab.protokt.shared.kotlin
import com.toasttab.protokt.shared.main
import io.codearte.gradle.nexus.NexusStagingExtension
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
import org.gradle.plugins.signing.SigningExtension

private object Pgp {
    val key by lazy {
        System.getenv("PGP_KEY")?.replace('$', '\n')
    }

    val password by lazy {
        System.getenv("PGP_PASSWORD")
    }
}

private object Remote {
    val username by lazy {
        System.getenv("OSSRH_USERNAME")
    }

    val password by lazy {
        System.getenv("OSSRH_PASSWORD")
    }

    val url = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

fun MavenPublication.standardPom() {
    pom {
        scm {
            url.set("https://github.com/open-toast/protokt")
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}

fun Project.sign(publication: MavenPublication) {
    configure<SigningExtension> {
        useInMemoryPgpKeys(Pgp.key, Pgp.password)
        sign(publication)
    }
}

fun Project.enablePublishing(defaultJars: Boolean = true) {
    apply(plugin = "maven-publish")

    val publishToRemote = !version.toString().endsWith("-SNAPSHOT")

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "integration"
                setUrl("${project.rootProject.buildDir}/repos/integration")
            }

            if (publishToRemote) {
                maven {
                    name = "remote"
                    setUrl(Remote.url)
                    credentials {
                        username = Remote.username
                        password = Remote.password
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
                    standardPom()
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
        enabled = publishToRemote
        group = "publishing"

        if (enabled) {
            val publishingExtension = project.the<PublishingExtension>()

            publishingExtension.publications.withType<MavenPublication> {
                sign(this)
            }

            dependsOn(tasks.withType<PublishToMavenRepository>().matching {
                it.repository == publishingExtension.repositories.getByName("remote")
            })
        }
    }
}

fun Project.promoteStagingRepo() {
    apply(plugin = "io.codearte.nexus-staging")

    configure<NexusStagingExtension> {
        username = Remote.username
        password = Remote.password
    }
}
