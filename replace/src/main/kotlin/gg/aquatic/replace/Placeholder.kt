package gg.aquatic.replace

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

/**
 * A generic placeholder that defines a template for replacing or formatting text using a specific
 * transformation function. The `Placeholder` class is designed to work with a type `T`, and its
 * transformation logic is determined by the provided lambda function `func`.
 *
 * @param T The type associated with the placeholder, used for transformation during text processing.
 * @property identifier A unique identifier for the placeholder, useful for distinguishing between
 *                       different placeholders in a collection.
 * @property isConst A flag indicating whether the placeholder should be treated as constant. If
 *                   true, the transform function is immutable.
 * @param func A transformation function that defines how a given target of type `T` and a text
 *             input are used to produce the processed output.
 */
interface Placeholder<T> {
    val identifier: String
    val isConst: Boolean

    fun <R> map(mapper: (R) -> T): Placeholder<R>

    open class Literal<T>(
        override val identifier: String,
        override val isConst: Boolean = false,
        private val func: (T, String) -> String
    ) : Placeholder<T> {
        fun apply(target: T, text: String): String = func(target, text)

        override fun <R> map(mapper: (R) -> T): Placeholder<R> {
            return Literal(identifier, isConst) { r, str -> func(mapper(r), str) }
        }
    }

    class Component<T>(
        override val identifier: String,
        override val isConst: Boolean = false,
        private val func: (T, String) -> net.kyori.adventure.text.Component
    ) : Placeholder<T> {
        fun apply(target: T, text: String): net.kyori.adventure.text.Component = func(target, text)

        override fun <R> map(mapper: (R) -> T): Placeholder<R> {
            return Component(identifier, isConst) { r, str -> func(mapper(r), str) }
        }
    }

    object PAPIPlaceholder : Literal<Player>("papi", false, { target, text ->
        val internalIdentifier = text.removePrefix("papi_")
        PlaceholderAPI.setPlaceholders(target, "%$internalIdentifier%")
    })
}