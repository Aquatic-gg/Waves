package gg.aquatic.waves.fake.entity.data

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure

object EntityClassLookup {

    fun searchForEntityDataClasses(namespace: String): Collection<Class<EntityData>> {
        val list = ArrayList<Class<EntityData>>()
        val reflections = Reflections(
            namespace, Scanners.SubTypes, Scanners.TypesAnnotated
        )

        val allClasses = reflections.getAll(Scanners.SubTypes)
            .filter { it.startsWith(namespace) }.mapNotNull {
                try {
                    Class.forName(it)
                } catch (e: ClassNotFoundException) {
                    null
                }
            }

        // Process each class
        for (clazz in allClasses) {
            // Check if the class itself is a subtype of EntityData
            if (EntityData::class.java.isAssignableFrom(clazz) && !clazz.isInterface) {
                @Suppress("UNCHECKED_CAST")
                list.add(clazz as Class<EntityData>)
                continue
            }

            // Check declared classes (inner/nested classes)
            for (innerClass in clazz.declaredClasses) {
                if (EntityData::class.java.isAssignableFrom(innerClass) && !innerClass.isInterface) {
                    @Suppress("UNCHECKED_CAST")
                    list.add(innerClass as Class<EntityData>)
                }
            }
        }

        return list
    }

    /**
     * Creates an instance of the specified EntityData class
     * Supports both Kotlin objects and regular classes with no-arg constructors
     *
     * @param entityDataClass The class to instantiate
     * @return An instance of the EntityData class or null if instantiation fails
     */
    fun createEntityDataInstance(entityDataClass: Class<EntityData>): EntityData? {
        try {
            // First check if it's a Kotlin object (singleton)
            try {
                val instanceField = entityDataClass.getDeclaredField("INSTANCE")
                instanceField.isAccessible = true
                return instanceField.get(null) as? EntityData
            } catch (e: NoSuchFieldException) {
                // Not a Kotlin object, continue to other approaches
            }

            return null

            /*

            // Check if it has a companion object with getInstance() or similar method
            val kotlinClass = entityDataClass.kotlin
            val companion = kotlinClass.companionObject
            if (companion != null) {
                val companionInstance = kotlinClass.companionObjectInstance
                if (companionInstance != null) {
                    // Look for common factory methods in companion object
                    val factoryMethods = listOf("getInstance", "create", "newInstance", "get")

                    for (methodName in factoryMethods) {
                        val method = companion.memberFunctions.find {
                            it.name == methodName &&
                                    it.parameters.size == 1 && // Just the receiver
                                    it.returnType.jvmErasure.java.let { returnClass ->
                                        EntityData::class.java.isAssignableFrom(returnClass)
                                    }
                        }

                        if (method != null) {
                            return method.call(companionInstance) as? EntityData
                        }
                    }
                }
            }

            // Use no-arg constructor for regular classes
            val constructor = entityDataClass.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
             */

        } catch (e: Exception) {
            // Log exception for debugging purposes
            println("Failed to create instance of ${entityDataClass.name}: ${e.message}")
        }

        return null
    }



    /**
     * Invokes a method defined in the EntityData interface on an instance of the specified EntityData class
     *
     * @param entityDataClass The class implementing EntityData interface
     * @param methodName The name of the method to invoke (must be defined in EntityData interface)
     * @param params Parameters to pass to the method
     * @return The result of method invocation or null if failed
     */
    fun invokeMethodOnEntityData(entityDataClass: Class<EntityData>, methodName: String, vararg params: Any?): Any? {
        try {
            // First, verify that the method exists in the EntityData interface
            val interfaceMethod = try {
                EntityData::class.java.getMethod(methodName, *params.map { it?.javaClass ?: Any::class.java }.toTypedArray())
            } catch (e: NoSuchMethodException) {
                return null // Method not defined in EntityData interface
            }

            // Check if class is a Kotlin object (has INSTANCE field)
            val instanceField = entityDataClass.fields.find { it.name == "INSTANCE" }
            if (instanceField != null) {
                val instance = instanceField.get(null)

                // Get method from the actual implementation class
                val method = entityDataClass.getMethod(methodName, *interfaceMethod.parameterTypes)
                return method.invoke(instance, *params)
            } else {
                // Try to find a companion object with the method
                val kotlinClass = entityDataClass.kotlin
                val companion = kotlinClass.companionObject

                if (companion != null) {
                    val companionInstance = kotlinClass.companionObjectInstance
                    val method = companion.memberFunctions.find {
                        it.name == methodName && it.parameters.size - 1 == params.size
                    }

                    if (method != null && companionInstance != null) {
                        return method.call(companionInstance, *params)
                    }
                }

                // If not a Kotlin object or companion, try to create a new instance
                try {
                    val instance = entityDataClass.getDeclaredConstructor().newInstance()
                    val method = entityDataClass.getMethod(methodName, *interfaceMethod.parameterTypes)
                    return method.invoke(instance, *params)
                } catch (e: Exception) {
                    // Cannot instantiate or method not found
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // For debugging
            return null
        }
        return null
    }
}