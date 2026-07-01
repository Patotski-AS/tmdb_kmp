package pas.tmdb.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tmdb.cmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("koin-compose").get())

                    implementation(libs.findLibrary("decompose").get())
                    implementation(libs.findLibrary("decompose-compose").get())
                    implementation(libs.findLibrary("essenty-lifecycle").get())
                    implementation(libs.findLibrary("essenty-lifecycle-coroutines").get())

                    implementation(libs.findLibrary("mvikotlin").get())
                    implementation(libs.findLibrary("mvikotlin-main").get())
                    implementation(libs.findLibrary("mvikotlin-coroutines").get())
                }
            }
        }
    }
}
