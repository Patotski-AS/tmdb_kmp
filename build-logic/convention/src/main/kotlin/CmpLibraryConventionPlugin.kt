package pas.tmdb.buildlogic

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tmdb.kmp.library")
        pluginManager.apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
        pluginManager.apply(libs.findPlugin("composeCompiler").get().get().pluginId)

        extensions.configure<LibraryExtension> {
            compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.findLibrary("compose-runtime").get())
                    implementation(libs.findLibrary("compose-foundation").get())
                    implementation(libs.findLibrary("compose-ui").get())
                    implementation(libs.findLibrary("compose-material3").get())
                    implementation(libs.findLibrary("compose-uiToolingPreview").get())
                }
                androidMain.dependencies {
                    implementation(libs.findLibrary("compose-uiTooling").get())
                }
            }
        }
    }
}
