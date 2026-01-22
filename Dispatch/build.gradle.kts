plugins {
    kotlin("jvm") version "2.2.21"
}

group = "gg.aquatic.dispatch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}