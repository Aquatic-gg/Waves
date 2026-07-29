plugins {
    kotlin("jvm") version "2.3.20"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.klocale"
version = "26.0.2"

repositories {
    mavenCentral()
}

dependencies {
}

tasks.test {
    useJUnitPlatform()
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    version = rootProject.version

    kotlin {
        jvmToolchain(25)
    }
}
