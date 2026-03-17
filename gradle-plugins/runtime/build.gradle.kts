plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("com.gradle.plugin-publish") version "2.1.0" apply false
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

allprojects {
    group = "gg.aquatic"
    version = "26.0.8"

    repositories {
        mavenCentral()
    }
}
