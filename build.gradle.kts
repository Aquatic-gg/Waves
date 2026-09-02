import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    id("gg.aquatic.bukkitkobjects")
    alias(libs.plugins.dotenv)
    alias(libs.plugins.librarian)
    java
    alias(libs.plugins.run.paper)
    `maven-publish`
    `java-library`

    // Applied selectively by individual modules.
    alias(libs.plugins.paperweight.userdev) apply false
    alias(libs.plugins.jmh) apply false
    alias(libs.plugins.jmhreport) apply false
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
    ":pakket:pakket-nms-26-2" to "pakket-nms-26-2",
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

java {
    withSourcesJar()
}

tasks {
    runServer {
        minecraftVersion("26.2")
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

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.slf4j.api)

    implementation(libs.librarian.paper)
    api(project(":kmenu:kmenu-core"))
    api(project(":kmenu:kmenu-serialization"))
    api(project(":replace"))
    api(project(":stacked"))
    api(project(":kregistry"))
    api(project(":pakket"))
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
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))
    api(project(":execute"))
    api(project(":kevent"))
    api(project(":kommand:kommand-core"))
    api(project(":kommand:kommand-paper"))
    api(project(":snapshot-map"))
    api(project(":tree-papi"))

    librarian(libs.kaml)

    librarian(libs.caffeine)
    librarian(libs.reflections)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.adventure.text.serializer.gson)
    compileOnly(libs.adventure.text.serializer.plain)
    compileOnly(libs.vault.api)
    compileOnly(libs.model.engine)
    compileOnly(libs.placeholderapi)

    // Testing
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
    testImplementation(libs.h2)

    // DB
    librarian(libs.exposed.core)
    librarian(libs.exposed.dao)
    librarian(libs.exposed.jdbc)
    librarian(libs.jedis)
    librarian(libs.hikaricp)
    librarian(libs.sqlite.jdbc)
    librarian(libs.mariadb.java.client)

    @Suppress("RedundantKotlinStdLibDependency")
    librarian(libs.kotlin.stdlib)
    librarian(libs.kotlin.reflect)
    librarian(libs.kotlinx.coroutines.core)
    librarian(libs.kotlinx.serialization.json)
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
