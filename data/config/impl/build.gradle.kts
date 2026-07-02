import java.util.Properties

plugins {
    alias(libs.plugins.tmdb.kmp.library)
}

val localProperties = rootProject.file("local.properties")
val localProps = Properties().apply {
    if (localProperties.exists()) {
        load(localProperties.inputStream())
    }
}
val tmdbAccessToken: String = localProps.getProperty("TMDB_ACCESS_TOKEN")
    ?: localProps.getProperty("TMDB_API_KEY", "")

val generateTmdbAccessToken = tasks.register("generateTmdbAccessToken") {
    val outputDir = layout.buildDirectory.dir("generated/tmdbAccessToken")
    val outputFile = outputDir.map { it.file("pas/tmdb/data/config/impl/internal/TmdbAccessToken.kt") }

    inputs.property("tmdbAccessToken", tmdbAccessToken)
    if (localProperties.exists()) {
        inputs.file(localProperties)
    }
    outputs.file(outputFile)

    doLast {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package pas.tmdb.data.config.impl.internal

            internal const val TMDB_ACCESS_TOKEN: String = ${tmdbAccessToken.toKotlinStringLiteral()}
            """.trimIndent(),
        )
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/tmdbAccessToken"))
        }
        commonMain.dependencies {
            implementation(projects.data.config.api)
            implementation(projects.core.network)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
        }
    }
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn(generateTmdbAccessToken)
}

private fun String.toKotlinStringLiteral(): String = buildString {
    append('"')
    for (char in this@toKotlinStringLiteral) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}
