plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.common"
version = "26.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:1.7.25")
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
            artifactId = "Common"
            version = "${project.version}"
            from(components["java"])
        }
    }
}