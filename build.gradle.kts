import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
    `maven-publish`
    java
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("co.uzzu.dotenv.gradle") version "2.0.0"

    id("xyz.jpenilla.gremlin-gradle") version "0.0.7"
}

group = "gg.aquatic.waves"
version = "1.2.4"

val ktor_version: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}


repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.oraxen.com/releases")
    maven("https://jitpack.io")
    maven {
        url = uri("https://repo.nekroplex.com/releases")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.auxilor.io/repository/maven-public/")
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.18.0")
    implementation("gg.aquatic.wavessync:wavessync-api:1.0.1:all")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.3.12")
    implementation("io.ktor:ktor-client-auth:$ktor_version")

    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.github.retrooper:packetevents-spigot:2.7.0")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("gg.aquatic:AEAPI:1.0")
    compileOnly("io.th0rgal:oraxen:1.171.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3-b")
    compileOnly ("com.ticxo.modelengine:ModelEngine:R4.0.8")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    implementation("com.github.micartey:webhookly:master-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.20.0")
    implementation("net.kyori:adventure-api:4.20.0")
    implementation("com.github.technicallycoded:FoliaLib:main-SNAPSHOT")
    compileOnly("com.willfp:eco:6.74.5")
    implementation("org.bstats:bstats-bukkit:3.1.0")

    //implementation("net.wesjd:anvilgui:1.10.4-SNAPSHOT")
}


kotlin {
    jvmToolchain(21)
}

configurations.compileOnly {
    extendsFrom(configurations.runtimeDownload.get())
}
configurations.testImplementation {
    extendsFrom(configurations.runtimeDownload.get())
}

tasks.register<ShadowJar>("shadowJarPlugin") {
    archiveFileName.set("Waves-${project.version}-Shaded.jar")
    archiveClassifier.set("plugin")


    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())


    //relocate("kotlinx.coroutines", "gg.aquatic.waves.shadow.kotlinx.coroutines")
    relocate("com.github.retrooper", "gg.aquatic.waves.shadow.com.retrooper")
    relocate("io.github.retrooper", "gg.aquatic.waves.shadow.io.retrooper")
    //relocate("kotlin", "gg.aquatic.waves.shadow.kotlin")

    // Exclude the original (unrelocated) kotlinx-coroutines-core package
    //exclude("kotlin/**")
    exclude("kotlin/**", "kotlinx/**","io/ktor/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")

    relocate("com.tcoded.folialib", "gg.aquatic.waves.shadow.lib.folialib")
    relocate("net.wesjd.anvilgui", "gg.aquatic.waves.shadow.net.wesjd.anvilgui")

    exclude("com/google/**","com/typesafe/**", "io/netty/**", "org/slf4j/**")
    exclude("plugin.yml")
    relocate("org.bstats", "gg.aquatic.waves.shadow.bstats")
}

tasks {
    build {
        dependsOn(named("shadowJarPlugin"))
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }
}

val maven_username = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val maven_password = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri("https://repo.nekroplex.com/releases")

            credentials {
                username = maven_username
                password = maven_password
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic.waves"
            artifactId = "Waves"
            version = "${project.version}"
            from(components["java"])
            artifact(tasks["shadowJarPlugin"]) {
                classifier = "plugin"
            }
        }
    }
}

