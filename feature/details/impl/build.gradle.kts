plugins {
    alias(libs.plugins.tmdb.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.details.api)
            implementation(projects.core.ui)
        }
    }
}
