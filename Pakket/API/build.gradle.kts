plugins {
    `maven-publish`
}

group = "gg.aquatic.pakket"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.9.Final")
    api("gg.aquatic:KEvent:26.0.5")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
    api("gg.aquatic:Common:26.0.13") {
        isChanging = true
    }
    api("gg.aquatic:Blokk:26.0.2")
    api("gg.aquatic:Stacked:26.0.3")
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
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
