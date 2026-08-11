plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.1.1"
}

description = "Gradle plugin that generates runtime dependency manifests and syncs Shadow relocations."

dependencies {
    compileOnly(gradleApi())
    compileOnly("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Jar>().configureEach {
    manifest.attributes("Implementation-Version" to project.version)
}

val maven_username = extra["mavenUsername"] as String
val maven_password = extra["mavenPassword"] as String

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri(
                "https://repo.aquatic.gg/" +
                    if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
            )

            credentials {
                username = maven_username
                password = maven_password
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
gradlePlugin {
    website.set("https://github.com/MrLarkyy/Runtime")
    vcsUrl.set("https://github.com/MrLarkyy/Runtime")
    plugins {
        create("runtime") {
            id = "gg.aquatic.runtime"
            displayName = "Aquatic Runtime"
            description = "Generates a runtime dependency manifest and applies Shadow relocations."
            implementationClass = "gg.aquatic.dependency.DependencyGeneratorPlugin"
            tags.set(listOf("runtime", "dependencies", "shadow"))
        }
    }
}
