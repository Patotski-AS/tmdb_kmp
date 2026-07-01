plugins {
    alias(libs.plugins.tmdb.kmp.feature.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.login.api)
            implementation(projects.auth.api)
            implementation(projects.core.ui)
        }
    }
}
