plugins {
    kotlin("jvm") version "2.3.20"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
    `java-library`
}

group = "gg.aquatic.common"
version = "26.0.16"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.nekroplex.com/releases")
}

val exposedVersion = "1.2.0"
dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    api("org.slf4j:slf4j-api:2.0.17")

    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    api("gg.aquatic:Dispatch:26.0.5")
    api("gg.aquatic:Dispatch-bukkit:26.0.5")
    api("com.zaxxer:HikariCP:7.0.2")
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
