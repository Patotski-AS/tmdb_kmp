package pas.tmdb.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpDataImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tmdb.kmp.library")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("ktor-client-core").get())
                    implementation(libs.findLibrary("ktor-client-content-negotiation").get())
                    implementation(libs.findLibrary("ktor-serialization-json").get())
                }
                androidMain.dependencies {
                    implementation(libs.findLibrary("ktor-client-okhttp").get())
                }
                iosMain.dependencies {
                    implementation(libs.findLibrary("ktor-client-darwin").get())
                }
                sourceSets.getByName("desktopMain").dependencies {
                    implementation(libs.findLibrary("ktor-client-cio").get())
                }
            }
        }
    }
}
