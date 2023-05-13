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

import com.toasttab.protokt.v1.gradle.isMultiplatform
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
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

object ProtoktProjectInfo {
    const val name = "Protokt"
    const val url = "https://github.com/open-toast/protokt"
    const val description = "Protobuf compiler and runtime for Kotlin"
}

fun Project.isRelease() = !version.toString().endsWith("-SNAPSHOT")

fun Project.enablePublishing(defaultJars: Boolean = true) {
    apply(plugin = "com.vanniktech.maven.publish.base")

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "integration"
                setUrl("${project.rootProject.buildDir}/repos/integration")
            }
        }
    }

    if (defaultJars) {
        configure<MavenPublishBaseExtension> {
            if (isMultiplatform()) {
                configure(KotlinMultiplatform(JavadocJar.Empty()))
            } else {
                configure(KotlinJvm(JavadocJar.Javadoc()))
            }
            publishToMavenCentral(SonatypeHost.DEFAULT)
            pom {
                name.set(ProtoktProjectInfo.name)
                description.set(ProtoktProjectInfo.description)
                url.set(ProtoktProjectInfo.url)
                scm { url.set(ProtoktProjectInfo.url) }
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

        afterEvaluate {
            if (tasks.findByName("generateProto") != null) {
                tasks.withType<Jar> {
                    dependsOn("generateProto")
                }
            }
        }
    }

    if (isRelease()) {
        apply(plugin = "signing")

        configure<SigningExtension> {
            useInMemoryPgpKeys(Pgp.key, Pgp.password)

            the<PublishingExtension>()
                .publications
                .withType<MavenPublication>()
                .forEach(::sign)
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
    }
}
