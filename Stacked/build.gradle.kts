plugins {
    kotlin("jvm") version "2.3.0"
}

group = "gg.aquatic.stacked"
version = "26.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "aquatic-releases"
        url = uri("https://repo.nekroplex.com/releases")
    }
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.oraxen.com/releases")
    maven("https://repo.auxilor.io/repository/maven-public/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven("https://repo.nexomc.com/releases")
    maven("https://repo.momirealms.net/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("gg.aquatic:KRegistry:25.0.1")
    compileOnly("gg.aquatic:KEvent:1.0.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Item adapters
    compileOnly("gg.aquatic:AEAPI:1.0")
    compileOnly("io.th0rgal:oraxen:1.203.2")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3")
    compileOnly("io.lumine:Mythic-Dist:5.11.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.willfp:eco:6.77.2")
    compileOnly("net.momirealms:craft-engine-core:0.0.66")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.66")
    compileOnly("com.nexomc:nexo:1.17.0")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}