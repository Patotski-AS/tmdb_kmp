package pas.tmdb.buildlogic

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("androidLibrary").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("kotlinSerialization").get().get().pluginId)

        extensions.configure<LibraryExtension> {
            compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            defaultConfig {
                minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
            }
            namespace = defaultNamespace()
        }

        extensions.configure<KotlinMultiplatformExtension> {
            configureTmdbTargets(target)
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
                }
            }
        }
    }
}
