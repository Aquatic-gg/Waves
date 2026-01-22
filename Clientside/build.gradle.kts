plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.clientside"
version = "26.0.1"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("gg.aquatic:Common:26.0.10") {
        isChanging = true
    }
    compileOnly("gg.aquatic.execute:Execute:26.0.1")
    compileOnly("gg.aquatic:snapshotmap:26.0.2")
    compileOnly("gg.aquatic:Pakket:26.1.6")
    compileOnly("gg.aquatic:Dispatch:26.0.1")
    compileOnly("gg.aquatic:KEvent:1.0.4")
    compileOnly("gg.aquatic:Blokk:26.0.2")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    testImplementation(kotlin("test"))
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
            artifactId = "Clientside"
            version = "${project.version}"

            from(components["java"])
        }
    }
}