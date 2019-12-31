import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.lint() {
    apply(plugin = "com.diffplug.gradle.spotless")

    configure<SpotlessExtension> {
        kotlin {
            ktlint()
            target("**/*.kt")
            targetExclude("**/generated-sources/**")
        }

        kotlinGradle {
            ktlint()
        }
    }
}
