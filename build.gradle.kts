import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.10"
    `maven-publish`
    java
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("co.uzzu.dotenv.gradle") version "2.0.0"
    id("xyz.jpenilla.gremlin-gradle") version "0.0.7"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
}

group = "gg.aquatic.waves"
version = "1.2.22"

val ktor_version: String by project

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.runtimeDownload.get())
    }
    testImplementation {
        extendsFrom(configurations.runtimeDownload.get())
    }
}


tasks.writeDependencies {
    relocate("com.zaxxer.hikari", "gg.aquatic.waves.libs.hikari")
    relocate("org.jetbrains.kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("kotlinx", "gg.aquatic.waves.libs.kotlinx")
    relocate("org.openjdk.nashorn", "gg.aquatic.waves.libs.nashorn")
}


gremlin {
    //defaultRepositories.set(false) // Optional: if you want to manage repositories manually
    repositories {
        maven("https://repo1.maven.org/maven2/") // Maven Central
        maven("https://maven.radsteve.net/public")
        maven {
            url = uri("https://repo.codemc.io/repository/maven-releases/")
        }
        maven {
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }
    }

    dependencies {
        // Define your dependencies
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.10")
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
        implementation("org.openjdk.nashorn:nashorn-core:15.4")
        implementation("com.zaxxer:HikariCP:5.1.0")
        implementation("net.radstevee.packed:packed-core:1.1.1")
        implementation("net.radstevee.packed:packed-negative-spaces:1.1.1")
        implementation("org.reflections:reflections:0.10.2")
    }
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
    mavenLocal()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.oraxen.com/releases")
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
    maven("https://maven.radsteve.net/public")
    maven("https://repo.nexomc.com/releases")
}

dependencies {
    implementation("xyz.jpenilla:gremlin-runtime:0.0.7")
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
    implementation("org.reflections:reflections:0.10.2")

    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("gg.aquatic:AEAPI:1.0")
    compileOnly("io.th0rgal:oraxen:1.171.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3-b")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
    compileOnly("io.lumine:Mythic-Dist:5.9.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    implementation("com.github.micartey:webhookly:master-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.20.0")
    implementation("net.kyori:adventure-api:4.20.0")
    compileOnly("com.willfp:eco:6.74.5")
    compileOnly("io.github.toxicity188:BetterModel:1.8.0")
    implementation("org.bstats:bstats-bukkit:3.1.0")

    runtimeDownload("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.10")
    runtimeDownload("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
    runtimeDownload("org.openjdk.nashorn:nashorn-core:15.4")
    runtimeDownload("com.zaxxer:HikariCP:5.1.0")
    runtimeDownload("net.radstevee.packed:packed-core:1.1.1")
    runtimeDownload("net.radstevee.packed:packed-negative-spaces:1.1.1")

    compileOnly("com.nexomc:nexo:1.8.0") //Nexo 1.X -> 1.X.0
    compileOnly("io.netty:netty-all:4.1.24.Final")

    implementation(project(":API"))
    implementation(project(":NMS_1_21_1"))
    implementation(project(":NMS_1_21_4"))
    implementation(project(":NMS_1_21_5"))
    implementation(project(":NMS_1_21_7"))
}

sourceSets {
    main {
        kotlin {
            srcDir("src/main/kotlin")
        }
        java {
            srcDir("src/main/java")
        }
    }
}


kotlin {
    jvmToolchain(21)
}

tasks.register<ShadowJar>("shadowJarPlugin") {
    archiveFileName.set("Waves-${project.version}-Shaded.jar")
    archiveClassifier.set("plugin")

    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())

    exclude("net/radstevee/**")

    exclude("com/zaxxer/**")
    exclude("kotlin/**", "kotlinx/**", "io/ktor/**", "assets/mappings/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")

    relocate("com.tcoded.folialib", "gg.aquatic.waves.shadow.lib.folialib")
    relocate("net.wesjd.anvilgui", "gg.aquatic.waves.shadow.net.wesjd.anvilgui")

    exclude("com/google/**", "com/typesafe/**", "io/netty/**", "org/slf4j/**")
    exclude("plugin.yml")
    relocate("org.bstats", "gg.aquatic.waves.shadow.bstats")

    relocate("kotlinx", "gg.aquatic.waves.libs.kotlinx")
    relocate("org.jetbrains.kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("kotlin", "gg.aquatic.waves.libs.kotlin")

    relocate("com.zaxxer.hikari", "gg.aquatic.waves.libs.hikari")

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
    )
}

tasks.register<ShadowJar>("shadowJarPublish") {
    archiveFileName.set("Waves-${project.version}-Publish.jar")
    archiveClassifier.set("publish")

    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())

    exclude("net/radstevee/**")

    exclude("com/zaxxer/**")
    exclude("kotlin/**", "kotlinx/**", "io/ktor/**", "assets/mappings/**")
    exclude("org/intellij/**")
    exclude("org/jetbrains/**")

    relocate("com.tcoded.folialib", "gg.aquatic.waves.shadow.lib.folialib")
    relocate("net.wesjd.anvilgui", "gg.aquatic.waves.shadow.net.wesjd.anvilgui")

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
    )

    exclude("com/google/**", "com/typesafe/**", "io/netty/**", "org/slf4j/**")
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
        filesMatching("paper-plugin.yml") {
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
            artifact(tasks["shadowJarPublish"]) {
                classifier = "publish"
            }

            artifact(tasks["shadowJarPlugin"]) {
                classifier = "plugin"
            }
        }
    }
}

