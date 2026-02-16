plugins {
    kotlin("jvm") version "2.3.10"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `java-library`
    `maven-publish`
}

group = "gg.aquatic.dispatch"
version = "26.0.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.slf4j:slf4j-api:2.0.17")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

val maven_username = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val maven_password = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
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
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "Dispatch"
            version = "${project.version}"
            from(components["java"])
        }
    }
}
