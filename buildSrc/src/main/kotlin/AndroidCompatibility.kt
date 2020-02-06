import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import ru.vyarus.gradle.plugin.animalsniffer.AnimalSnifferPlugin

fun Project.compatibleWithAndroid(api: Int = 19) {
    apply<AnimalSnifferPlugin>()

    dependencies {
        add("signature", "com.toasttab.android:gummy-bears-api-$api:0.1.0@signature")
    }
}
