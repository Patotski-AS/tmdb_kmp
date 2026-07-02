plugins {
    alias(libs.plugins.tmdb.kmp.feature.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.media.api)
            implementation(projects.feature.details.api)
            implementation(projects.feature.login.api)
        }
    }
}
