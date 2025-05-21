plugins {
    kotlin("jvm")
}

group = "gg.aquatic.waves.api"
version = parent!!.version

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.24.Final")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
}

kotlin {
    jvmToolchain(21)
}