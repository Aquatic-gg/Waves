plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "gg.aquatic"
version = "2.0.0-SNAPSHOT"

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

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.20")
}

gradlePlugin {
    plugins {
        create("bukkit-kobjects") {
            id = "gg.aquatic.bukkitkobjects"
            displayName = "Bukkit KObjects"
            description = "A Gradle plugin that allows using Kotlin objects for JavaPlugins"
            implementationClass = "revxrsal.kobjects.KObjectPlugin"
            website = "https://github.com/Revxrsal/bukkit-kobjects"
            vcsUrl = "https://github.com/Revxrsal/bukkit-kobjects.git"
            tags = listOf("kotlin", "asm", "spigot", "bukkit", "minecraft")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri(
                "https://repo.aquatic.gg/" +
                    if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
            )

            credentials {
                username = secret("MAVEN_USERNAME")
                password = secret("MAVEN_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
