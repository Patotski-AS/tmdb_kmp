pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "tmdb_kmp"

include(":app")
include(":androidApp")
include(":desktopApp")

include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":core:network")

include(":auth:api")
include(":auth:impl")

include(":data:config:api")
include(":data:config:impl")
include(":data:storage:api")
include(":data:storage:impl")
include(":data:tmdb:api")
include(":data:tmdb:impl")

include(":feature:login:api")
include(":feature:login:impl")
include(":feature:main:api")
include(":feature:main:impl")
include(":feature:discover:api")
include(":feature:discover:impl")
include(":feature:details:api")
include(":feature:details:impl")
include(":feature:search:api")
include(":feature:search:impl")
