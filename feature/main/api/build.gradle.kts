plugins {
    alias(libs.plugins.tmdb.kmp.feature.api)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.discover.api)
            implementation(projects.feature.details.api)
            implementation(projects.feature.search.api)
            implementation(projects.feature.login.api)
        }
    }
}
