package gg.aquatic.execute.argument

import org.bukkit.Color
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ArgumentContext<T>(
    val binder: T,
    val arguments: ObjectArguments,
    val updater: (T, String) -> String
) {

    // Property delegate implementations for different types

    /**
     * Gets a string value from arguments with the updater automatically applied.
     */
    fun string(id: String): String? {
        return arguments.string(id) { str -> updatedValue(str) }
    }

    /**
     * Gets an int value from arguments with the updater automatically applied.
     */
    fun int(id: String): Int? {
        return arguments.int(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a double value from arguments with the updater automatically applied.
     */
    fun double(id: String): Double? {
        return arguments.double(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a boolean value from arguments with the updater automatically applied.
     */
    fun boolean(id: String): Boolean? {
        return arguments.boolean(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a float value from arguments with the updater automatically applied.
     */
    fun float(id: String): Float? {
        return arguments.float(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a long value from arguments with the updater automatically applied.
     */
    fun long(id: String): Long? {
        return arguments.long(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a short value from arguments with the updater automatically applied.
     */
    fun short(id: String): Short? {
        return arguments.short(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a byte value from arguments with the updater automatically applied.
     */
    fun byte(id: String): Byte? {
        return arguments.byte(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a vector value from arguments with the updater automatically applied.
     */
    fun vector(id: String): Vector? {
        return arguments.vector(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a vector3f value from arguments with the updater automatically applied.
     */
    fun vector3f(id: String): Vector3f? {
        return arguments.vector3f(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a color value from arguments with the updater automatically applied.
     */
    fun color(id: String): Color? {
        return arguments.color(id) { str -> updatedValue(str) }
    }

    /**
     * Gets any value from arguments with the updater automatically applied if appropriate.
     */
    fun any(id: String): Any? {
        return arguments.any(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a typed value from arguments with the updater automatically applied if appropriate.
     */
    inline fun <reified R> typed(id: String): R? {
        return arguments.typed<R>(id) { str -> updatedValue(str) }
    }

    /**
     * Gets a collection of strings from arguments with the updater automatically applied.
     */
    fun stringCollection(id: String): Collection<String>? {
        return arguments.stringCollection(id) { str -> updatedValue(str) }
    }

    /**
     * Applies the updater function to the given string if a binder is available.
     */
    fun updatedValue(str: String): String {
        return updater(binder, str)
    }

    // Property delegation operators

    /**
     * Smart provideDelegate operator that returns the appropriate delegate based on the property type.
     * This allows syntax like: val double: Double? by context
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <R> provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): ReadOnlyProperty<Any?, R?> {
        val id = property.name
        val returnType = property.returnType

        // Handle only nullable types to match your API design
        if (returnType.isMarkedNullable) {
            return when (returnType.classifier) {
                String::class -> StringDelegate(id) as ReadOnlyProperty<Any?, R?>
                Int::class -> IntDelegate(id) as ReadOnlyProperty<Any?, R?>
                Double::class -> DoubleDelegate(id) as ReadOnlyProperty<Any?, R?>
                Boolean::class -> BooleanDelegate(id) as ReadOnlyProperty<Any?, R?>
                Float::class -> FloatDelegate(id) as ReadOnlyProperty<Any?, R?>
                Long::class -> LongDelegate(id) as ReadOnlyProperty<Any?, R?>
                Short::class -> ShortDelegate(id) as ReadOnlyProperty<Any?, R?>
                Byte::class -> ByteDelegate(id) as ReadOnlyProperty<Any?, R?>
                Vector::class -> VectorDelegate(id) as ReadOnlyProperty<Any?, R?>
                Vector3f::class -> Vector3fDelegate(id) as ReadOnlyProperty<Any?, R?>
                Color::class -> ColorDelegate(id) as ReadOnlyProperty<Any?, R?>
                else -> object : ReadOnlyProperty<Any?, R?> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>): R? {
                        return any(id) as? R
                    }
                }
            }
        }

        // For non-nullable types (which is unusual in your API), fall back to a generic delegate
        return object : ReadOnlyProperty<Any?, R?> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): R? {
                return any(id) as? R
            }
        }
    }

    // Delegate classes

    inner class StringDelegate(private val id: String) : ReadOnlyProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
            return string(id)
        }
    }

    inner class IntDelegate(private val id: String) : ReadOnlyProperty<Any?, Int?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return int(id)
        }
    }

    inner class DoubleDelegate(private val id: String) : ReadOnlyProperty<Any?, Double?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Double? {
            return double(id)
        }
    }

    inner class BooleanDelegate(private val id: String) : ReadOnlyProperty<Any?, Boolean?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? {
            return boolean(id)
        }
    }

    inner class FloatDelegate(private val id: String) : ReadOnlyProperty<Any?, Float?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Float? {
            return float(id)
        }
    }

    inner class LongDelegate(private val id: String) : ReadOnlyProperty<Any?, Long?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Long? {
            return long(id)
        }
    }

    inner class ShortDelegate(private val id: String) : ReadOnlyProperty<Any?, Short?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Short? {
            return short(id)
        }
    }

    inner class ByteDelegate(private val id: String) : ReadOnlyProperty<Any?, Byte?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Byte? {
            return byte(id)
        }
    }

    inner class VectorDelegate(private val id: String) : ReadOnlyProperty<Any?, Vector?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Vector? {
            return vector(id)
        }
    }

    inner class Vector3fDelegate(private val id: String) : ReadOnlyProperty<Any?, Vector3f?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Vector3f? {
            return vector3f(id)
        }
    }

    inner class ColorDelegate(private val id: String) : ReadOnlyProperty<Any?, Color?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Color? {
            return color(id)
        }
    }

    inner class AnyDelegate(private val id: String) : ReadOnlyProperty<Any?, Any?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Any? {
            return any(id)
        }
    }
}