package gg.aquatic.waves.util.generic

import gg.aquatic.waves.Waves
import gg.aquatic.waves.util.reflection.AnnotationLookup
import gg.aquatic.waves.util.reflection.GenericTypeResolver
import java.lang.reflect.Modifier

object ExecutableAnnotationProcessor {

    internal inline fun <T : ExecutableObject<*, *>, reified D : Annotation> process(
        plugin: Any,
        annotationClass: Class<D>,
        pckg: String,
        executableClass: Class<T>,
        idRetriever: (D) -> String,
        aliasesRetriever: (D) -> Array<out String>,
        register: (String, T, Class<*>) -> Unit,
        errorHandler: AnnotationLookupErrorHandler,
    ) {
        val logger = Waves.INSTANCE.logger
        logger.info("Trying to register Executables in the $pckg package... Please make sure your classes are annotated with the @${annotationClass.name} annotation!")
        val annotatedClasses = AnnotationLookup.lookup<D>(plugin, pckg)
        logger.info("Found ${annotatedClasses.size} classes with @${annotationClass.name}")
        for (clazz in annotatedClasses) {
            try {
                val annotation = clazz.getAnnotation(annotationClass) ?: continue
                val id = idRetriever(annotation)
                val aliases = aliasesRetriever(annotation)

                if (Modifier.isAbstract(clazz.modifiers) || clazz.isInterface) {
                    errorHandler.handleNonClass(clazz)
                    //logger.warning("Failed to register ${clazz.name}: is abstract or an interface")
                    continue
                }

                val instance = createInstance(clazz)
                if (instance == null) {
                    errorHandler.handleInstanceCreationFailure(clazz)
                    //logger.warning("Failed to create instance of ${clazz.name}")
                    continue
                }
                if (instance is ExecutableObject<*, *>) {
                    val binderClass =
                        GenericTypeResolver.findGenericParameter(clazz, ExecutableObject::class.java, 0) ?: continue
                    register(id, executableClass.cast(instance) ?: continue, binderClass)
                    for (string in aliases) {
                        register(string, executableClass.cast(instance) ?: continue, binderClass)
                    }
                    logger.info("Registered executable: $id ${if (aliases.isNotEmpty()) "[${aliases.joinToString(", ")}]" else ""} for ${clazz.simpleName}<${binderClass.simpleName}>")
                } else {
                    logger.warning("Failed to register ${clazz.name}: is not an instance of ${executableClass.name}")
                }
            } catch (e: Exception) {
                errorHandler.handleTryCatch(clazz, e)
                //logger.warning("Failed to register ${clazz.name}: ${e.message}")
                e.printStackTrace()
            }

        }
    }

    internal inline fun <T : ExecutableObject<*, *>, reified D : Annotation> process(
        plugin: Any,
        annotationClass: Class<D>,
        pckg: String,
        executableClass: Class<T>,
        idRetriever: (D) -> String,
        aliasesRetriever: (D) -> Array<out String>,
        register: (String, T, Class<*>) -> Unit,
        noinline errorHandler: ErrorHandlerBuilder.() -> Unit,
    ) {
        process(
            plugin,
            annotationClass,
            pckg,
            executableClass,
            idRetriever,
            aliasesRetriever,
            register,
            this.errorHandler(errorHandler)
        )
    }

    interface AnnotationLookupErrorHandler {
        fun handleNonClass(clazz: Class<*>)
        fun handleInstanceCreationFailure(clazz: Class<*>)
        fun handleTryCatch(clazz: Class<*>, e: Exception)
    }

    fun errorHandler(errorHandlerBuilder: ErrorHandlerBuilder.() -> Unit): AnnotationLookupErrorHandler {
        val builder = ErrorHandlerBuilder()
        errorHandlerBuilder(builder)
        return builder.build()
    }

    class ErrorHandlerBuilder() {
        private var nonClassHandler: ((Class<*>) -> Unit)? = null
        private var instanceCreationFailureHandler: ((Class<*>) -> Unit)? = null
        private var tryCatchHandler: ((Class<*>, Exception) -> Unit)? = null

        fun onNonClass(handler: (Class<*>) -> Unit) = apply { nonClassHandler = handler }
        fun onInstanceCreationFailure(handler: (Class<*>) -> Unit) = apply { instanceCreationFailureHandler = handler }
        fun onTryCatch(handler: (Class<*>, Exception) -> Unit) = apply { tryCatchHandler = handler }

        fun build(): AnnotationLookupErrorHandler {
            return object : AnnotationLookupErrorHandler {
                override fun handleNonClass(clazz: Class<*>) {
                    nonClassHandler?.invoke(clazz)
                }

                override fun handleInstanceCreationFailure(clazz: Class<*>) {
                    instanceCreationFailureHandler?.invoke(clazz)
                }

                override fun handleTryCatch(clazz: Class<*>, e: Exception) {
                    tryCatchHandler?.invoke(clazz, e)
                }
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