import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.3.0"
    `maven-publish`
    java
    id("com.gradleup.shadow") version "9.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("xyz.jpenilla.gremlin-gradle") version "0.0.9"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" apply false
}

group = "gg.aquatic.waves"
version = "1.3.30"

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
    relocate("org.bstats", "gg.aquatic.waves.shadow.bstats")
    //relocate("com.undefined", "gg.aquatic.waves.shadow.undefined")
}


gremlin {
    //defaultRepositories.set(false) // Optional: if you want to manage repositories manually
    repositories {
        maven {
            name = "undefined-repo"
            url = uri("https://repo.undefinedcreations.com/releases")
        }
        maven {
            name = "undefined-snapshots"
            url = uri("https://repo.undefinedcreations.com/snapshots")
        }
        maven("https://jitpack.io")
        maven("https://repo1.maven.org/maven2/") // Maven Central
        maven("https://maven.radsteve.net/public")
        maven {
            url = uri("https://repo.codemc.io/repository/maven-releases/")
        }
        maven {
            url = uri("https://repo.codemc.io/repository/maven-snapshots/")
        }

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    dependencies {
        // Define your dependencies
        implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
        implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
        implementation("org.openjdk.nashorn:nashorn-core:15.7")
        implementation("com.zaxxer:HikariCP:7.0.2")
        implementation("net.radstevee.packed:packed-core:1.1.2")
        implementation("net.radstevee.packed:packed-negative-spaces:1.1.2")
        implementation("org.reflections:reflections:0.10.2")
        implementation("com.github.micartey:webhookly:master-SNAPSHOT")
        implementation("net.kyori:adventure-text-minimessage:4.26.1")
        implementation("org.bstats:bstats-bukkit:3.1.0")
        implementation("net.kyori:adventure-text-serializer-plain:4.26.1")
        implementation("net.kyori:adventure-text-serializer-gson:4.26.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("com.undefined:stellar-kotlin:1.1.3-SNAPSHOT:paper")
        implementation("com.undefined:stellar:1.1.3-SNAPSHOT:paper")
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
    maven("https://repo.momirealms.net/releases/")
    maven {
        name = "undefined-repo"
        url = uri("https://repo.undefinedcreations.com/releases")
    }
    maven {
        name = "undefined-repo"
        url = uri("https://repo.undefinedcreations.com/snapshots")
    }
}

dependencies {
    implementation("xyz.jpenilla:gremlin-runtime:0.0.9")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.7")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("gg.aquatic:AEAPI:1.0")
    compileOnly("io.th0rgal:oraxen:1.203.2")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    compileOnly("io.lumine:Mythic-Dist:5.11.1")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.willfp:eco:6.77.2")
    compileOnly("io.github.toxicity188:bettermodel:1.15.1")

    runtimeDownload("org.reflections:reflections:0.10.2")
    runtimeDownload("com.github.micartey:webhookly:master-SNAPSHOT")
    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
    runtimeDownload("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
    runtimeDownload("org.openjdk.nashorn:nashorn-core:15.7")
    runtimeDownload("com.zaxxer:HikariCP:7.0.2")
    runtimeDownload("net.radstevee.packed:packed-core:1.1.2")
    runtimeDownload("net.radstevee.packed:packed-negative-spaces:1.1.2")
    runtimeDownload("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-api:4.26.1")
    runtimeDownload("org.bstats:bstats-bukkit:3.1.0")
    runtimeDownload("net.kyori:adventure-text-serializer-gson:4.26.1")
    runtimeDownload("net.kyori:adventure-text-serializer-plain:4.26.1")
    runtimeDownload("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    runtimeDownload("com.undefined:stellar-kotlin:1.2.0-SNAPSHOT:paper")
    runtimeDownload("com.undefined:stellar:1.2.0-SNAPSHOT:paper")

    compileOnly("net.momirealms:craft-engine-core:0.0.66")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.66")

    compileOnly("com.nexomc:nexo:1.17.0") //Nexo 1.X -> 1.X.0
    compileOnly("io.netty:netty-all:4.1.24.Final")

    implementation(project(":API"))
    implementation(project(":NMS_1_21_1"))
    implementation(project(":NMS_1_21_4"))
    implementation(project(":NMS_1_21_5"))
    implementation(project(":NMS_1_21_7"))
    implementation(project(":NMS_1_21_9"))
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

    exclude("com/google/**", "com/typesafe/**", "io/netty/**", "org/slf4j/**")
    exclude("plugin.yml")

    exclude("com/googlecode/**")
    exclude("com/**")

    exclude("javassist/**")
    exclude("javax/**")
    exclude("me/**")
    exclude("net/**")
    exclude("org/**")

    exclude("_COROUTINE")
    exclude("OSGI-INF")

    relocate("org.bstats", "gg.aquatic.waves.shadow.bstats")
    relocate("kotlinx", "gg.aquatic.waves.libs.kotlinx")
    relocate("org.jetbrains.kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("kotlin", "gg.aquatic.waves.libs.kotlin")

    //relocate("com.undefined", "gg.aquatic.waves.shadow.undefined")

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

    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
    )

    exclude("com/google/**", "com/typesafe/**", "io/netty/**", "org/slf4j/**")
    exclude("plugin.yml")
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
            expand(mutableMapOf("version" to version))
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

