plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("com.gradle.plugin-publish") version "1.3.1" apply false
}

allprojects {
    group = "gg.aquatic"
    version = "26.0.2"

    repositories {
        mavenCentral()
    }
}
