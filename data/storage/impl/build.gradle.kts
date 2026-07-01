plugins {
    alias(libs.plugins.tmdb.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.storage.api)
            implementation(libs.koin.core)
        }
    }
}
