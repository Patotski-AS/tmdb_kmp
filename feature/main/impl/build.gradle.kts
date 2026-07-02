plugins {
    alias(libs.plugins.tmdb.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.main.api)
            implementation(projects.feature.media.api)
            implementation(projects.feature.details.api)
            implementation(projects.feature.login.api)
            implementation(projects.core.ui)
        }
    }
}
