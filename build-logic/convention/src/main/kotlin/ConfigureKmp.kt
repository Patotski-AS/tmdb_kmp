package pas.tmdb.buildlogic

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformExtension.configureTmdbTargets(
    project: Project,
    exportIosFramework: Boolean = false,
) {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm("desktop")

    if (exportIosFramework) {
        listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "TmdbApp"
                isStatic = true
            }
        }
    }

    applyDefaultHierarchyTemplate()
}
