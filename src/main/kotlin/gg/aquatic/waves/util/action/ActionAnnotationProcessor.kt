package gg.aquatic.waves.util.action

import gg.aquatic.waves.Waves
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.util.reflection.AnnotationLookup
import gg.aquatic.waves.util.reflection.GenericTypeResolver
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ExecutableObject
import java.lang.reflect.Modifier

object ActionAnnotationProcessor {

    fun process(pckg: String) {
        val logger = Waves.INSTANCE.logger
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
                if (instance is ExecutableObject<*,*>) {
                    val binderClass = GenericTypeResolver.findGenericParameter(clazz, ExecutableObject::class.java, 0) ?: continue
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

    private fun <T> createInstance(clazz: Class<T>): T? {
        // First check if it's a Kotlin object (singleton)
        try {
            val instanceField = clazz.getDeclaredField("INSTANCE")
            instanceField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            return instanceField.get(null) as? T
        } catch (e: NoSuchFieldException) {
            // Not a Kotlin object, continue to other approaches
        }

        // Try no-arg constructor
        return try {
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (e: Exception) {
            null
        }
    }

}