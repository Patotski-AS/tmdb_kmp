plugins {
    alias(libs.plugins.tmdb.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.discover.api)
            implementation(projects.core.ui)
        }
    }
}
