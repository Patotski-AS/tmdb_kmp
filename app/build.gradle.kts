plugins {
    alias(libs.plugins.tmdb.kmp.app)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.ui)
            implementation(projects.core.navigation)
            implementation(projects.auth.api)
            implementation(projects.auth.impl)
            implementation(projects.data.config.impl)
            implementation(projects.data.storage.impl)
            implementation(projects.data.tmdb.impl)
            implementation(projects.feature.login.api)
            implementation(projects.feature.login.impl)
            implementation(projects.feature.main.api)
            implementation(projects.feature.main.impl)
            implementation(projects.feature.discover.api)
            implementation(projects.feature.discover.impl)
            implementation(projects.feature.details.api)
            implementation(projects.feature.details.impl)
            implementation(projects.feature.search.api)
            implementation(projects.feature.search.impl)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.decompose.compose)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}
