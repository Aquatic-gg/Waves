import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "gg.aquatic.kommand"
version = "26.1.0"

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven("https://libraries.minecraft.net")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "aquaticRepository"
                url = uri("https://repo.nekroplex.com/releases")

                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "gg.aquatic"
                artifactId = project.name
                version = rootProject.version.toString()
                from(components["java"])
            }
        }
    }
}
