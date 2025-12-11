plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "io.github.revxrsal"
version = "0.0.5"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.0")
    implementation("org.ow2.asm:asm:9.9")
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
