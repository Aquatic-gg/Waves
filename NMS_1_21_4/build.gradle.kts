plugins {
    kotlin("jvm")
    id("com.undefinedcreations.echo") version "0.0.11"
}

group = "gg.aquatic.waves.nms"
version = parent!!.version

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":API"))
    echo(
        "1.21.4",
        generateSource = false,
        generateDocs = false
    )
    compileOnly("net.kyori:adventure-api:4.20.0")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.20.0")
}
kotlin {
    jvmToolchain(21)
}