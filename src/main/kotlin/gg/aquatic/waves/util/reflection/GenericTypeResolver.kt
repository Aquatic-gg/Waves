package gg.aquatic.waves.util.reflection

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object GenericTypeResolver {

    fun findGenericParameter(clazz: Class<*>, targetGenericClass: Class<*>, paramIndex: Int): Class<*>? {
        // Check all generic interfaces first
        for (genericInterface in clazz.genericInterfaces) {
            val typeParameter = findTypeParameter(genericInterface, targetGenericClass, paramIndex)
            if (typeParameter != null) {
                return typeParameter
            }
        }

        // Then check superclass
        val genericSuperclass = clazz.genericSuperclass
        if (genericSuperclass != null) {
            val typeParameter = findTypeParameter(genericSuperclass, targetGenericClass, paramIndex)
            if (typeParameter != null) {
                return typeParameter
            }

            // Try to find in parent class
            val superclass = clazz.superclass
            if (superclass != null) {
                return findGenericParameter(superclass, targetGenericClass, paramIndex)
            }
        }

        return null
    }

    private fun findTypeParameter(type: Type, targetGenericClass: Class<*>, paramIndex: Int): Class<*>? {
        if (type is ParameterizedType) {
            val rawType = type.rawType
            if (rawType is Class<*> && targetGenericClass.isAssignableFrom(rawType)) {
                val typeArgs = type.actualTypeArguments
                if (typeArgs.size > paramIndex) {
                    val paramType = typeArgs[paramIndex]
                    if (paramType is Class<*>) {
                        return paramType
                    }
                    // Handle nested parameterized types
                    if (paramType is ParameterizedType) {
                        return paramType.rawType as? Class<*>
                    }
                }
            }
        }
        return null
    }


}