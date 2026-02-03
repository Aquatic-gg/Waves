plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    `java-library`
    `maven-publish`
}

group = "gg.aquatic.pakket.nms"

repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")
    compileOnly(project(":API"))
    compileOnly("gg.aquatic:KEvent:26.0.5")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
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
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
