package gg.aquatic.waves.util.requirement

import RegisterRequirement
import gg.aquatic.waves.Waves
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.generic.Condition
import gg.aquatic.waves.util.generic.ExecutableAnnotationProcessor
import java.util.concurrent.ConcurrentHashMap

object RequirementAnnotationProcessor {
    fun process(plugin: Any,pckg: String) {
        val logger = Waves.INSTANCE.logger
        ExecutableAnnotationProcessor.process(
            plugin,
            RegisterRequirement::class.java,
            pckg,
            Condition::class.java,
            { ann -> ann.id },
            { ann -> ann.aliases},
            { id, inst, binderClass ->
                val map = WavesRegistry.REQUIREMENT.getOrPut(binderClass) { ConcurrentHashMap() }
                if (map.containsKey(id)) {
                    logger.warning("Requirement with ID of $id has a duplicated Key! Such Requirement ID was already used by ${map[id]!!.javaClass.name}")
                }
                map += id to inst
            }) {

            this.onNonClass {
                logger.warning("Failed to register ${it.name}: is abstract or an interface")
            }
            this.onInstanceCreationFailure {
                logger.warning("Failed to create instance of ${it.name}")
            }
            this.onTryCatch { clazz, e ->
                logger.warning("Failed to register ${clazz.name} executable")
                e.printStackTrace()
            }
        }
    }
}