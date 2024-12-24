package revxrsal.kobjects

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import revxrsal.kobjects.asm.ClassRewriter
import java.io.File

class KObjectPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            error("Bukkit-KObjects requires the Kotlin plugin to work with")
        }

        project.extensions.create("bukkitKObjects", KObjectExtension::class.java)

        val transform = project.tasks.register("transformBukkitObjects") {
            group = "build"
            description = "Performs bytecode transformations to Kotlin object to make them usable by Bukkit"

            project.tasks["classes"].doLast {
                val classesToTransform = project.bukkitKObjects.classes
                if (classesToTransform.isEmpty())
                    error("You must specify at least 1 class to transform in the bukkitKObject block.")
                val destination = project.kotlinDestinationDirectory
                val classFiles = classesToTransform.map {
                    val file = destination.resolve(it.replace('.', '/') + ".class")
                    if (file.exists())
                        return@map file
                    error("Couldn't find file '$file' in any of the following directories: $destination")
                }
                for (objectClass in classFiles) {
                    ClassRewriter.rewrite(objectClass)
                }
            }
        }

        project.tasks.withType<KotlinJvmCompile> {
            dependsOn(transform)
        }
    }
}

private val Project.kotlinDestinationDirectory: File
    get() {
        val kotlin = project.extensions.getByType(KotlinProjectExtension::class.java)
        val mainSourceSet = kotlin.sourceSets.getByName("main")
        val kotlinSourceDirs = mainSourceSet.kotlin.destinationDirectory
        return kotlinSourceDirs.asFile.get()
    }
