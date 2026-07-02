plugins {
    alias(libs.plugins.tmdb.kmp.feature.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.auth.api)
        }
    }
}
