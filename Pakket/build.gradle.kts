plugins {
    kotlin("jvm") version "2.3.20"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
    `maven-publish`
    `java-library`
}

group = "gg.aquatic"
version = "26.1.10"

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
    api(project(":API"))
    api(project(":NMS_1_21_4"))
    api(project(":NMS_1_21_8"))
    api(project(":NMS_1_21_9"))
    api("gg.aquatic:KEvent:26.0.5")
    api("gg.aquatic:Common:26.0.16")
}

kotlin {
    jvmToolchain(21)
}

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        maven("https://repo.nekroplex.com/releases")
        maven("https://mvn.lumine.io/repository/maven-public/")
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    kotlin {
        jvmToolchain(21)
    }

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
    }
}

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
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
