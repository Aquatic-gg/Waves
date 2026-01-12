plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.blokk"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
    maven("https://maven.devs.beer/")
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("gg.aquatic:KRegistry:25.0.1")
    compileOnly("gg.aquatic:Common:26.0.3")
    testImplementation(kotlin("test"))

    // Implementations
    compileOnly("dev.lone:api-itemsadder:4.0.10")
    compileOnly("io.th0rgal:oraxen:1.203.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}