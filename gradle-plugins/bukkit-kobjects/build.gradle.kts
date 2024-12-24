plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.revxrsal"
version = "0.0.4"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.20")
    implementation("org.ow2.asm:asm:9.7")
}

gradlePlugin {
    plugins {
        create("bukkit-kobjects") {
            id = "io.github.revxrsal.bukkitkobjects"
            displayName = "Bukkit KObjects"
            description = "A Gradle plugin that allows using Kotlin objects for JavaPlugins"
            implementationClass = "revxrsal.kobjects.KObjectPlugin"
            website = "https://github.com/Revxrsal/bukkit-kobjects"
            vcsUrl = "https://github.com/Revxrsal/bukkit-kobjects.git"
            tags = listOf("kotlin", "asm", "spigot", "bukkit", "minecraft")
        }
    }
}
