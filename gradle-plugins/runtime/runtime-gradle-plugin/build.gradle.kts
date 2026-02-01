plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    `maven-publish`
}

description = "Gradle plugin that generates runtime dependency manifests and syncs Shadow relocations."

dependencies {
    compileOnly(gradleApi())
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

gradlePlugin {
    website.set("https://github.com/johndoe/GradlePlugins")
    vcsUrl.set("https://github.com/johndoe/GradlePlugins")
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
