plugins {
    kotlin("jvm") version "2.3.0" apply false
    id("com.gradle.plugin-publish") version "1.3.1" apply false
}

allprojects {
    group = "gg.aquatic"
    version = "26.0.1"

    repositories {
        mavenCentral()
    }
}
