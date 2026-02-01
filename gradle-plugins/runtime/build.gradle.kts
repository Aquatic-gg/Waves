plugins {
    kotlin("jvm") version "2.2.21" apply false
    id("com.gradle.plugin-publish") version "2.0.0" apply false
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

allprojects {
    group = "gg.aquatic"
    version = "26.0.5"

    repositories {
        mavenCentral()
    }
}
