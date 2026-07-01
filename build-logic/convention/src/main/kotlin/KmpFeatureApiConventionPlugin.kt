package pas.tmdb.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tmdb.kmp.api")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("decompose").get())
                }
            }
        }
    }
}
