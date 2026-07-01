package pas.tmdb.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class AppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("tmdb.kmp.feature.impl")

        extensions.configure<KotlinMultiplatformExtension> {
            configureTmdbTargets(target, exportIosFramework = true)
        }
    }
}
