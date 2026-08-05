plugins {
    java
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "io.github.revxrsal"
version = "0.1.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.0")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.20")
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
