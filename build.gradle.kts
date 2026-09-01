import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("com.gradleup.shadow") version "9.6.1"
    id("gg.aquatic.bukkitkobjects")
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("xyz.kyngs.librarian.plugin") version "2.0.0-SNAPSHOT"
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    `maven-publish`
    `java-library`

    // Applied selectively by individual modules.
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("me.champeau.jmh") version "0.7.3" apply false
    id("io.morethan.jmhreport") version "0.9.6" apply false
}

bukkitKObjects {
    classes.add("gg.aquatic.waves.Waves")
}

group = "gg.aquatic.waves"
version = "2.0.0-SNAPSHOT"

/**
 * Maven artifact id published for each module, keyed by Gradle project path.
 *
 * Projects absent from this map (the `:kmenu`, `:kholograms`, `:klocale` and
 * `:kommand` containers) hold no sources and publish nothing.
 */
val publishedArtifactIds = mapOf(
    ":aquatic-common" to "aquatic-common",
    ":blokk" to "blokk",
    ":clientside" to "clientside",
    ":dispatch" to "dispatch",
    ":dispatch:dispatch-paper" to "dispatch-paper",
    ":execute" to "execute",
    ":kevent" to "kevent",
    ":kholograms:kholograms-core" to "kholograms",
    ":kholograms:kholograms-serialization" to "kholograms-serialization",
    ":klocale:klocale-common" to "klocale",
    ":klocale:klocale-paper" to "klocale-paper",
    ":kmenu:kmenu-core" to "kmenu",
    ":kmenu:kmenu-serialization" to "kmenu-serialization",
    ":kommand:kommand-core" to "kommand-core",
    ":kommand:kommand-paper" to "kommand-paper",
    ":kommand:kommand-velocity" to "kommand-velocity",
    ":kregistry" to "kregistry",
    ":kurrency" to "kurrency",
    ":pakket" to "pakket",
    ":pakket:pakket-api" to "pakket-api",
    ":pakket:pakket-nms-1-21-4" to "pakket-nms-1-21-4",
    ":pakket:pakket-nms-1-21-8" to "pakket-nms-1-21-8",
    ":pakket:pakket-nms-1-21-9" to "pakket-nms-1-21-9",
    ":pakket:pakket-nms-26-1-1" to "pakket-nms-26-1-1",
    ":quick-mini-message" to "quick-mini-message",
    ":replace" to "replace",
    ":snapshot-map" to "snapshot-map",
    ":stacked" to "stacked",
    ":statistik" to "statistik",
    ":tree-papi" to "tree-papi",
)

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

val isSnapshot = version.toString().endsWith("-SNAPSHOT")
val publishRepositoryUrl = "https://repo.aquatic.gg/" + if (isSnapshot) "snapshots" else "releases"

fun RepositoryHandler.aquaticRepository() = maven {
    name = "aquaticRepository"
    url = uri(publishRepositoryUrl)

    credentials {
        username = mavenUsername
        password = mavenPassword
    }
    authentication {
        create<BasicAuthentication>("basic")
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "aquatic-releases"
            url = uri("https://repo.aquatic.gg/releases")
        }
        maven("https://jitpack.io")
        maven("https://libraries.minecraft.net")
        maven("https://maven.devs.beer/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.momirealms.net/releases")
        maven("https://repo.nexomc.com/releases")
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.kyngs.xyz/public/")
    }
}

subprojects {
    val moduleArtifactId = publishedArtifactIds[path] ?: return@subprojects

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "gg.aquatic"
    version = rootProject.version

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    extensions.configure<PublishingExtension> {
        repositories {
            aquaticRepository()
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "gg.aquatic"
                artifactId = moduleArtifactId
                version = rootProject.version.toString()

                from(components["java"])
            }
        }
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf(
            "version" to project.version
        )
        inputs.properties(props)
        filesMatching("*.yml") {
            expand(props)
        }
    }

    test {
        useJUnitPlatform()
    }
}

val exposedVersion = "1.2.0"
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.+")
    compileOnly("org.slf4j:slf4j-api:2.0.17")

    implementation("xyz.kyngs.librarian:librarian-paper:2.0.0-SNAPSHOT")
    api(project(":kmenu:kmenu-core"))
    api(project(":kmenu:kmenu-serialization"))
    api(project(":replace"))
    api(project(":stacked"))
    api(project(":kregistry"))
    api(project(":pakket"))
    api(project(":execute"))
    api(project(":aquatic-common"))
    api(project(":kurrency"))
    api(project(":klocale:klocale-common"))
    api(project(":klocale:klocale-paper"))
    api(project(":blokk"))
    api(project(":statistik"))
    api(project(":kholograms:kholograms-core"))
    api(project(":kholograms:kholograms-serialization"))
    api(project(":clientside"))
    api(project(":quick-mini-message"))
    librarian("com.charleskorn.kaml:kaml:0.104.0")

    librarian("com.github.ben-manes.caffeine:caffeine:3.2.3")
    librarian("org.reflections:reflections:0.10.2")
    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-gson:4.26.1")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.26.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    compileOnly("me.clip:placeholderapi:2.12.2")

    // Testing
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.4.240")

    // DB
    librarian("org.jetbrains.exposed:exposed-core:$exposedVersion")
    librarian("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    librarian("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    librarian("redis.clients:jedis:7.4.1")
    librarian("com.zaxxer:HikariCP:7.0.2")
    librarian("org.xerial:sqlite-jdbc:3.53.0.0")
    librarian("org.mariadb.jdbc:mariadb-java-client:3.5.8")

    @Suppress("RedundantKotlinStdLibDependency")
    librarian("org.jetbrains.kotlin:kotlin-stdlib:2.3.20")
    librarian("org.jetbrains.kotlin:kotlin-reflect:2.3.20")
    librarian("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    librarian("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

val excludedLibs = listOf(
    "org.slf4j:.*:.*",
    "org.checkerframework:.*:.*",
    "com.google.errorprone:.*:.*",
    "com.google.protobuf:.*:.*",
    "com.google.code.gson:.*:.*",
)

librarian {
    excludedLibs.forEach { excludeDependency(it) }
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<ShadowJar> {
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
        exclude(dependency("org.jetbrains:annotations:.*"))
        exclude(dependency("com.intellij:annotations:.*"))

        exclude(dependency("net.kyori:adventure-api:.*"))
        exclude(dependency("org.javassist:javassist:.*"))
        exclude(dependency("javax.annotation:javax.annotation-api:.*"))
        exclude(dependency("com.google.code.findbugs:jsr305:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
        exclude(dependency("com.github.ben-manes.caffeine:caffeine:.*"))
        exclude(dependency("com.google.code.gson:gson:.*"))
        exclude(dependency("org.jetbrains.exposed:.*:.*"))
        exclude(dependency("com.zaxxer:HikariCP:.*"))
        exclude(dependency("redis.clients:jedis:.*"))
        exclude(dependency("org.apache.commons:commons-pool2:.*"))
        exclude(dependency("org.json:json:.*"))
    }

    relocate("kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("kotlinx", "gg.aquatic.waves.libs.kotlinx")
    relocate("org.jetbrains.kotlin", "gg.aquatic.waves.libs.kotlin")
    relocate("org.jetbrains.exposed", "gg.aquatic.waves.libs.exposed")
    relocate("com.zaxxer.hikari", "gg.aquatic.waves.libs.hikari")
    relocate("org.bstats", "gg.aquatic.waves.libs.bstats")

    mergeServiceFiles()
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    archiveBaseName.set("Waves")
    archiveClassifier.set("")
}

publishing {
    repositories {
        aquaticRepository()
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "waves"
            version = project.version.toString()
            from(components["java"])
        }
    }
}

// The bundled Gradle plugins live in separate included builds, so their publish
// tasks are not part of the root publish by default. Wire them in explicitly.
tasks.named("publish") {
    dependsOn(gradle.includedBuild("bukkit-kobjects").task(":publish"))
}
