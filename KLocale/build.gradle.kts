plugins {
    kotlin("jvm") version "2.3.0"
}

group = "gg.aquatic.klocale"
version = "26.0.1"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}