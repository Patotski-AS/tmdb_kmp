plugins {
    `kotlin-dsl`
}

group = "pas.tmdb.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "tmdb.kmp.library"
            implementationClass = "pas.tmdb.buildlogic.KmpLibraryConventionPlugin"
        }
        register("kmpApi") {
            id = "tmdb.kmp.api"
            implementationClass = "pas.tmdb.buildlogic.KmpApiConventionPlugin"
        }
        register("cmpLibrary") {
            id = "tmdb.cmp.library"
            implementationClass = "pas.tmdb.buildlogic.CmpLibraryConventionPlugin"
        }
        register("kmpDataImpl") {
            id = "tmdb.kmp.data.impl"
            implementationClass = "pas.tmdb.buildlogic.KmpDataImplConventionPlugin"
        }
        register("kmpFeatureApi") {
            id = "tmdb.kmp.feature.api"
            implementationClass = "pas.tmdb.buildlogic.KmpFeatureApiConventionPlugin"
        }
        register("kmpFeatureImpl") {
            id = "tmdb.kmp.feature.impl"
            implementationClass = "pas.tmdb.buildlogic.KmpFeatureImplConventionPlugin"
        }
        register("app") {
            id = "tmdb.kmp.app"
            implementationClass = "pas.tmdb.buildlogic.AppConventionPlugin"
        }
    }
}
