plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
}

description = "Gradle plugin that generates runtime dependency manifests and syncs Shadow relocations."

dependencies {
    compileOnly(gradleApi())
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
