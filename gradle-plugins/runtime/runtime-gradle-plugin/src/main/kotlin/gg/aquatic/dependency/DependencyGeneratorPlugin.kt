package gg.aquatic.dependency

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

open class DependencyExtension @Inject constructor(private val objects: ObjectFactory) {
    val repositories: ListProperty<RuntimeRepository> = objects.listProperty(RuntimeRepository::class.java)
    val relocations: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)

    init {
        repositories.convention(emptyList())
        relocations.convention(emptyMap())
    }

    fun repo(url: String, configure: RuntimeRepository.() -> Unit = {}) {
        val repo = objects.newInstance(RuntimeRepository::class.java)
        repo.url.set(url)
        repo.configure()
        repositories.add(repo)
    }

    fun relocate(from: String, to: String) = relocations.put(from, to)
}

abstract class RuntimeRepository @Inject constructor() {
    @get:Input
    abstract val url: Property<String>

    @get:Input
    abstract val username: Property<String>

    @get:Input
    abstract val password: Property<String>

    init {
        username.convention("")
        password.convention("")
    }

    fun credentials(user: String, pass: String) {
        username.set(user)
        password.set(pass)
    }
}

abstract class GenerateManifestTask : DefaultTask() {
    @get:Nested
    abstract val repositories: ListProperty<RuntimeRepository>

    @get:Input
    abstract val relocations: MapProperty<String, String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Internal
    lateinit var libraryConfiguration: Configuration

    @TaskAction
    fun generate() {
        // 1. Map Dependencies
        val deps = libraryConfiguration.resolvedConfiguration.resolvedArtifacts.map { artifact ->
            val id = artifact.moduleVersion.id
            mapOf(
                "group" to id.group,
                "artifact" to id.name,
                "version" to id.version,
                "checksum" to calculateChecksum(artifact.file)
            )
        }

        // 2. Map Repositories (including per-repo credentials)
        val repoData = repositories.get().map { repo ->
            mapOf(
                "url" to repo.url.get(),
                // We save the placeholder strings, NOT the actual secrets
                "user" to repo.username.get(),
                "pass" to repo.password.get()
            )
        }

        val manifest = mapOf(
            "repositories" to repoData,
            "dependencies" to deps,
            "relocations" to relocations.get().map { mapOf("from" to it.key, "to" to it.value) }
        )

        val output = outputFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(JsonOutput.toJson(manifest))

        project.logger.lifecycle("Generated dependency manifest with ${deps.size} dependencies and ${repoData.size} repositories.")
    }

    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(file.readBytes())
        return hash.joinToString("") { "%02x".format(it) }
    }
}

// 3. The Plugin
class DependencyGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<DependencyExtension>("dependencyResolution")

        // Create the configuration for runtime downloading
        val runtimeDownload = project.configurations.create("runtimeDownload") {
            isCanBeResolved = true
            isCanBeConsumed = false
        }

        // Register the manifest generator task
        val genTask = project.tasks.register<GenerateManifestTask>("generateManifest") {
            repositories.set(extension.repositories)
            relocations.set(extension.relocations)
            libraryConfiguration = runtimeDownload
            outputFile.set(project.layout.buildDirectory.file("generated/resources/runtime/dependencies.json"))
        }

        // Sync relocations with ShadowJar automatically
        project.afterEvaluate {
            val relocations = extension.relocations.get()

            if (relocations.isNotEmpty()) {
                if (project.plugins.hasPlugin("com.gradleup.shadow") || project.plugins.hasPlugin("com.github.johnrengelman.shadow")) {
                    // We look for the task by name and type generically to avoid classloading issues
                    project.tasks.matching { it.name == "shadowJar" }.configureEach {
                        val task = this
                        extension.relocations.get().forEach { (from, to) ->
                            // Use reflection to call 'relocate' so the ShadowJar class
                            // is only needed at runtime by Gradle, not by our plugin's classloader verification
                            try {
                                val relocateMethod = task.javaClass.getMethod("relocate", String::class.java, String::class.java)
                                relocateMethod.invoke(task, from, to)
                            } catch (e: Exception) {
                                project.logger.warn("Failed to apply relocation '$from' to 'shadowJar' task dynamically.")
                            }
                        }
                    }
                } else {
                    project.logger.warn("Relocations were specified, but no Shadow plugin ('com.gradleup.shadow' or 'com.github.johnrengelman.shadow') was found.")
                }
            }
        }

        project.plugins.withId("java") {
            // Include runtimeDownload in compilation classpaths
            project.configurations.named("compileOnly") {
                extendsFrom(runtimeDownload)
            }
            project.configurations.named("testImplementation") {
                extendsFrom(runtimeDownload)
            }

            // Ensure manifest is generated before resources are processed
            project.tasks.named("processResources", ProcessResources::class.java) {
                dependsOn(genTask)
                from(genTask.map { it.outputFile.get().asFile.parentFile })
            }
        }
    }
}

fun DependencyHandler.runtimeDownload(dependencyNotation: Any) = add("runtimeDownload", dependencyNotation)
