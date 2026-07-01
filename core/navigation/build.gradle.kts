plugins {
    alias(libs.plugins.tmdb.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)
            implementation(projects.feature.main.api)
        }
    }
}
