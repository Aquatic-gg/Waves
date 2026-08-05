package revxrsal.kobjects

import org.gradle.api.Action
import org.gradle.api.Project

/**
 * Returns the KObjects extension configuration
 */
val Project.bukkitKObjects get() = extensions.getByName("bukkitKObjects") as KObjectExtension

/**
 * Configures the KObjects plugin
 */
fun Project.bukkitKObjects(configure: Action<KObjectExtension>) {
    project.extensions.configure("bukkitKObjects", configure)
}
