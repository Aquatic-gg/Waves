plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.kholograms"
version = "26.0.1"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("gg.aquatic:Common:26.0.10") {
        isChanging = true
    }
    compileOnly("gg.aquatic:KRegistry:25.0.2")
    compileOnly("gg.aquatic.execute:Execute:26.0.1")
    compileOnly("gg.aquatic.replace:Replace:26.0.3")
    compileOnly("gg.aquatic:snapshotmap:26.0.2")
    compileOnly("gg.aquatic:Pakket:26.1.6")
    compileOnly("gg.aquatic:Stacked:26.0.2")
    compileOnly("gg.aquatic:TreePAPI:26.0.1")
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
            artifactId = "KHolograms"
            version = "${project.version}"

            from(components["java"])
        }
    }
}