plugins {
    kotlin("jvm") version "2.3.10" apply false
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "gg.aquatic.kholograms"
version = "26.0.1"

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

    val maven_username = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
    val maven_password = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

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
