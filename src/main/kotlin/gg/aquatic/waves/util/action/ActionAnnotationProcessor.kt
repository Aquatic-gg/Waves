package gg.aquatic.waves.util.action

import gg.aquatic.waves.Waves
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ExecutableAnnotationProcessor

object ActionAnnotationProcessor {

    fun process(pckg: String) {
        ExecutableAnnotationProcessor.process(
            RegisterAction::class.java,
            pckg,
            Action::class.java,
            { ann -> ann.id },
            { id, inst, binderClass ->
                WavesRegistry.ACTION.getOrPut(binderClass) { hashMapOf() } += id to inst
            }) {
            val logger = Waves.INSTANCE.logger

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

    /*
    fun process(pckg: String) {
        val logger = Waves.INSTANCE.logger
        logger.info("Trying to register Actions in the $pckg package... Please make sure your classes are annotated with the @RegisterAction annotation!")
        val annotatedClasses = AnnotationLookup.lookup<RegisterAction>(pckg)
        logger.info("Found ${annotatedClasses.size} classes with @RegisterAction")
        for (clazz in annotatedClasses) {
            try {
                val annotation = clazz.getAnnotation(RegisterAction::class.java) ?: continue
                val id = annotation.id

                if (Modifier.isAbstract(clazz.modifiers) || clazz.isInterface) {
                    logger.warning("Failed to register ${clazz.name}: is abstract or an interface")
                    continue
                }

                val instance = createInstance(clazz)
                if (instance == null) {
                    logger.warning("Failed to create instance of ${clazz.name}")
                    continue
                }
                if (instance is ExecutableObject<*, *>) {
                    val binderClass =
                        GenericTypeResolver.findGenericParameter(clazz, ExecutableObject::class.java, 0) ?: continue
                    val map = WavesRegistry.ACTION.getOrPut(binderClass) { hashMapOf() }
                    instance as? Action<*> ?: continue
                    map += id to instance
                    logger.info("Registered action: $id for ${binderClass.simpleName} -> ${clazz.simpleName}")
                }
            } catch (e: Exception) {
                logger.warning("Failed to register ${clazz.name}: ${e.message}")
                e.printStackTrace()
            }

        }
    }
     */


}