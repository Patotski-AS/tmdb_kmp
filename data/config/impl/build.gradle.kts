import java.util.Properties

plugins {
    alias(libs.plugins.tmdb.kmp.library)
}

val localProperties = rootProject.file("local.properties")
val tmdbApiKey: String = Properties().apply {
    if (localProperties.exists()) {
        load(localProperties.inputStream())
    }
}.getProperty("TMDB_API_KEY", "")

val generateTmdbApiKey = tasks.register("generateTmdbApiKey") {
    val outputDir = layout.buildDirectory.dir("generated/tmdbApiKey")
    val outputFile = outputDir.map { it.file("pas/tmdb/data/config/impl/internal/TmdbApiKey.kt") }

    inputs.property("tmdbApiKey", tmdbApiKey)
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

            internal const val TMDB_API_KEY: String = ${tmdbApiKey.toKotlinStringLiteral()}
            """.trimIndent(),
        )
    }
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/tmdbApiKey"))
        }
        commonMain.dependencies {
            implementation(projects.data.config.api)
            implementation(libs.koin.core)
        }
    }
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn(generateTmdbApiKey)
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
