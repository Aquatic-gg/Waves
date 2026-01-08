plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic"
version = "26.0.1"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":API"))
    implementation(project(":NMS_1_21_9"))
    compileOnly("gg.aquatic:KEvent:1.0.4")
}

kotlin {
    jvmToolchain(21)
}

subprojects {
    apply(plugin = "kotlin")

    version = rootProject.version
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    kotlin {
        jvmToolchain(21)
    }
}

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri("https://repo.nekroplex.com/releases")

            credentials {
                username = mavenUsername
                password = mavenPassword
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "Pakket"
            version = "${project.version}"

            from(components["java"])
            //artifact(tasks.compileJava)
        }
    }
}
