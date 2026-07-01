plugins {
    alias(libs.plugins.tmdb.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.details.api)
            implementation(projects.data.tmdb.api)
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(libs.coil.compose)
        }
    }
}
