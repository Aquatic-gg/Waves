plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("com.gradle.plugin-publish") version "2.1.1" apply false
}

// Publishing credentials are inherited from the repository root .env (shared
// with the main Waves build) or the process environment. This build is included
// as a separate composite build, so it resolves the root .env directly instead
// of through a dotenv plugin (which only sees this build's own directory).
val rootDotenv: Map<String, String> =
    generateSequence(rootDir) { it.parentFile }
        .map { it.resolve(".env") }
        .firstOrNull { it.isFile }
        ?.readLines()
        .orEmpty()
        .mapNotNull { line ->
            val entry = line.trim()
            if (entry.isEmpty() || entry.startsWith("#")) return@mapNotNull null
            val separator = entry.indexOf('=').takeIf { it > 0 } ?: return@mapNotNull null
            entry.substring(0, separator).trim() to entry.substring(separator + 1).trim().removeSurrounding("\"")
        }
        .toMap()

fun secret(name: String): String = System.getenv(name) ?: rootDotenv[name] ?: ""

allprojects {
    group = "gg.aquatic"
    version = "2.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extra["mavenUsername"] = secret("MAVEN_USERNAME")
    extra["mavenPassword"] = secret("MAVEN_PASSWORD")
}

// Aggregate lifecycle task so the composite root can publish this build with a
// single ":publish", matching Gradle's per-project publish task.
tasks.register("publish") {
    dependsOn(subprojects.map { "${it.path}:publish" })
}
