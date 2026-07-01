plugins {
    alias(libs.plugins.tmdb.kmp.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
