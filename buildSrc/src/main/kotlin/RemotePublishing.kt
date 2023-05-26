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

import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
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

object ProtoktProjectInfo {
    const val name = "Protokt"
    const val url = "https://github.com/open-toast/protokt"
    const val description = "Protobuf compiler and runtime for Kotlin"
}

fun MavenPublication.standardPom() {
    pom {
        name.set(ProtoktProjectInfo.name)
        description.set(ProtoktProjectInfo.description)
        url.set(ProtoktProjectInfo.url)
        scm {
            url.set(ProtoktProjectInfo.url)
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Toast")
                name.set("Toast Open Source")
                email.set("opensource@toasttab.com")
            }
        }
    }
}

fun Project.isRelease() = !version.toString().endsWith("-SNAPSHOT")

fun Project.enablePublishing(defaultJars: Boolean = true) {
    apply(plugin = "maven-publish")

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "integration"
                setUrl("${project.rootProject.buildDir}/repos/integration")
            }

            if (isRelease()) {
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
        configure<JavaPluginExtension> {
            withSourcesJar()
        }

        afterEvaluate {
            if (tasks.findByName("generateProto") != null) {
                tasks.named("sourcesJar").configure {
                    dependsOn("generateProto")
                }
            }
        }

        tasks.register<Jar>("javadocJar") {
            from("$rootDir/README.md")
            archiveClassifier.set("javadoc")
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("sources") {
                    from(components.getByName("java"))
                    artifact(tasks.getByName("javadocJar"))
                    artifactId = project.name
                    version = "${project.version}"
                    groupId = "$group"
                }
            }
        }
    }

    if (isRelease()) {
        apply(plugin = "signing")

        configure<SigningExtension> {
            useInMemoryPgpKeys(Pgp.key, Pgp.password)

            project.the<PublishingExtension>().publications.withType<MavenPublication> {
                standardPom()
                sign(this)
            }
        }
    }

    tasks.register("publishToIntegrationRepository") {
        group = "publishing"

        val publishingExtension = project.the<PublishingExtension>()

        dependsOn(
            tasks.withType<PublishToMavenRepository>().matching {
                it.repository == publishingExtension.repositories.getByName("integration")
            }
        )
        dependsOn(tasks.withType<Jar>())
    }

    tasks.register("publishToRemote") {
        enabled = isRelease()
        group = "publishing"

        if (enabled) {
            val publishingExtension = project.the<PublishingExtension>()

            dependsOn(
                tasks.withType<PublishToMavenRepository>().matching {
                    it.repository == publishingExtension.repositories.getByName("remote")
                }
            )
            dependsOn(tasks.withType<Jar>())
        }
    }
}

fun Project.promoteStagingRepo() {
    if (isRelease()) {
        apply(plugin = "io.codearte.nexus-staging")

        configure<NexusStagingExtension> {
            username = Remote.username
            password = Remote.password
            packageGroup = "com.toasttab"
            numberOfRetries = 50
        }

        tasks.named("closeRepository") {
            dependsOn(tasks.withType<PublishToMavenRepository>())
        }
    } else {
        tasks.register("closeAndReleaseRepository")
    }
}
