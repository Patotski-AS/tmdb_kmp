plugins {
    alias(libs.plugins.tmdb.kmp.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
        }
    }
}
