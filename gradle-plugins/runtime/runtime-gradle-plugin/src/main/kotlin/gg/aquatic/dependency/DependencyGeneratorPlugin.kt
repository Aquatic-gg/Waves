package gg.aquatic.dependency

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

@Suppress("unused")
open class DependencyExtension @Inject constructor(private val objects: ObjectFactory) {
    val repositories: ListProperty<RuntimeRepository> = objects.listProperty(RuntimeRepository::class.java)
    val relocations: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
    val addRuntimeCore: Property<Boolean> = objects.property(Boolean::class.java)
    val runtimeCoreVersion: Property<String> = objects.property(String::class.java)

    init {
        repositories.convention(emptyList())
        relocations.convention(emptyMap())
        addRuntimeCore.convention(true)
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

    @get:Input
    abstract val dependencyCoordinates: ListProperty<String>

    @get:Classpath
    abstract val dependencyFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Internal
    lateinit var libraryConfiguration: Configuration

    @TaskAction
    fun generate() {
        val deps = libraryConfiguration.resolvedConfiguration.resolvedArtifacts.map { artifact ->
            val id = artifact.moduleVersion.id
            mapOf(
                "group" to id.group,
                "artifact" to id.name,
                "version" to id.version,
                "checksum" to calculateChecksum(artifact.file)
            )
        }

        val repoData = repositories.get().map { repo ->
            mapOf(
                "url" to repo.url.get(),
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
        val pluginVersion = javaClass.`package`.implementationVersion

        // Create the configuration for runtime downloading
        val runtimeDownload = project.configurations.create("runtimeDownload") {
            isCanBeResolved = true
            isCanBeConsumed = false
        }

        // Register the manifest generator task
        val genTask = project.tasks.register<GenerateManifestTask>("generateManifest") {
            repositories.set(extension.repositories)
            relocations.set(extension.relocations)
            dependencyCoordinates.set(project.provider {
                runtimeDownload.resolvedConfiguration.resolvedArtifacts
                    .map { artifact ->
                        val id = artifact.moduleVersion.id
                        "${id.group}:${id.name}:${id.version}"
                    }
                    .sorted()
            })
            dependencyFiles.setFrom(runtimeDownload)
            libraryConfiguration = runtimeDownload
            outputFile.set(project.layout.buildDirectory.file("generated/resources/runtime/dependencies.json"))
        }

        // Sync relocations with ShadowJar automatically
        project.afterEvaluate {
            val relocations = extension.relocations.get()

            if (relocations.isNotEmpty()) {
                if (project.plugins.hasPlugin("com.gradleup.shadow") || project.plugins.hasPlugin("com.github.johnrengelman.shadow")) {
                    project.tasks.withType(ShadowJar::class.java).configureEach {
                        extension.relocations.get().forEach { (from, to) ->
                            try {
                                relocate(from, to)
                            } catch (e: Exception) {
                                project.logger.warn("Failed to apply relocation '$from' to 'shadowJar' task dynamically.")
                            }
                        }
                    }
                } else {
                    project.logger.warn("Relocations were specified, but no Shadow plugin ('com.gradleup.shadow' or 'com.github.johnrengelman.shadow') was found.")
                }
            }

            if (project.plugins.hasPlugin("com.gradleup.shadow") || project.plugins.hasPlugin("com.github.johnrengelman.shadow")) {
                val runtimeFiles = project.provider { runtimeDownload.resolve().toSet() }

                project.tasks.withType(ShadowJar::class.java).configureEach {
                    includedDependencies.setFrom(project.provider {
                        dependencyFilter.get()
                            .resolve(configurations.get())
                            .filter { it !in runtimeFiles.get() }
                    })
                }
            }
        }

        project.plugins.withId("java") {
            if (extension.addRuntimeCore.getOrElse(true)) {
                val resolvedVersion = extension.runtimeCoreVersion.orNull ?: pluginVersion
                if (resolvedVersion.isNullOrBlank()) {
                    project.logger.warn("Unable to determine runtime-core version. Set dependencyResolution.runtimeCoreVersion to force one.")
                } else {
                    val implementationConfig = project.configurations.getByName("implementation")
                    val alreadyAdded = implementationConfig.dependencies.any {
                        it.group == "gg.aquatic" && it.name == "runtime-core"
                    }
                    if (!alreadyAdded) {
                        project.dependencies.add(implementationConfig.name, "gg.aquatic:runtime-core:$resolvedVersion")
                    }
                }
            }

            project.configurations.named("compileOnly") {
                extendsFrom(runtimeDownload)
            }
            project.configurations.named("testImplementation") {
                extendsFrom(runtimeDownload)
            }

            project.tasks.withType(org.gradle.api.tasks.bundling.Jar::class.java).configureEach {
                dependsOn(genTask)
                from(genTask.map { it.outputFile.get().asFile })
            }
        }
    }
}

fun DependencyHandler.runtimeDownload(dependencyNotation: Any) = add("runtimeDownload", dependencyNotation)
