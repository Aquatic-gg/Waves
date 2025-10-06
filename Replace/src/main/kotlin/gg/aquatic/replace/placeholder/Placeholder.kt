package gg.aquatic.replace.placeholder

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
class Placeholder<T>(
    val identifier: String,
    val isConst: Boolean = false,
    private val func: (T, String) -> String
) {

    fun apply(target: T, text: String): String {
        return func(target, text)
    }
}