import org.gradle.api.publish.PublishingExtension

plugins {
    kotlin("jvm") version "2.3.0" apply false
}

group = "gg.aquatic.kholograms"
version = "26.0.1"

fun loadDotenv(rootDir: File): Map<String, String> {
    val file = rootDir.resolve(".env")
    if (!file.exists()) return emptyMap()
    return file.readLines()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) return@mapNotNull null
            val (key, value) = trimmed.split("=", limit = 2)
            key.trim() to value.trim()
        }
        .toMap()
}

val dotenv = loadDotenv(rootDir)
fun envValue(key: String): String = dotenv[key] ?: System.getenv(key).orEmpty()

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")

    repositories {
        maven("https://repo.nekroplex.com/releases")
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        mavenCentral()
        maven("https://jitpack.io")
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(21)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val maven_username = envValue("MAVEN_USERNAME")
    val maven_password = envValue("MAVEN_PASSWORD")

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "aquaticRepository"
                url = uri("https://repo.nekroplex.com/releases")

                credentials {
                    username = maven_username
                    password = maven_password
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}
