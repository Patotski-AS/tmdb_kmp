plugins {
    alias(libs.plugins.tmdb.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.coil.compose)
        }
    }
}
