plugins {
    alias(libs.plugins.tmdb.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
    }
}
