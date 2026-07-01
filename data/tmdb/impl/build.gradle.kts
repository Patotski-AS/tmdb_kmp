plugins {
    alias(libs.plugins.tmdb.kmp.data.impl)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.tmdb.api)
            implementation(projects.data.config.api)
            implementation(projects.auth.api)
            implementation(projects.core.common)
            implementation(projects.core.network)
        }
    }
}
