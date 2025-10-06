package gg.aquatic.replace.placeholder

import org.bukkit.entity.Player
import kotlin.collections.iterator

object Placeholders {

    val registered = hashMapOf<Class<*>, MutableCollection<Placeholder<*>>>()

    val player: PlaceholderContext<Player>
        get() {
            return resolverFor(Player::class.java)
        }

    /**
     * Resolves a `PlaceholderContext` for the specified type `T` by setting up a list of transforms
     * and a maximum update interval.
     *
     * @param T The type parameter for which the `PlaceholderContext` is resolved.
     * @param maxUpdateInterval The maximum update interval in seconds, defaults to 5.
     * @param transforms A variable number of transformations of type `Transform<T, *>` to be applied.
     * @return A `PlaceholderContext` configured for type `T` with the provided transformations and update interval.
     */
    inline fun <reified T> resolverFor(
        maxUpdateInterval: Int = 5,
        vararg transforms: Transform<T, *>
    ): PlaceholderContext<T> {
        return resolverFor(T::class.java, maxUpdateInterval, *transforms)
    }

    /**
     * Creates a new instance of `PlaceholderContext` for the given class type, using the provided
     * transforms and an optional maximum update interval.
     *
     * @param clazz The class type for which the placeholder context is to be resolved.
     * @param maxUpdateInterval The maximum interval for updates (in seconds). Defaults to 5.
     * @param transforms A variable number of transforms to be applied during the resolution process.
     * @return A `PlaceholderContext` containing the resolved placeholders for the specified class type.
     */
    fun <T> resolverFor(
        clazz: Class<T>,
        maxUpdateInterval: Int = 5,
        vararg transforms: Transform<T, *>
    ): PlaceholderContext<T> {
        val placeholders = ArrayList<Placeholder<T>>()

        val original = getRegisteredFor(clazz)
        for (placeholder in original) {
            placeholders.add(placeholder as Placeholder<T>)
        }

        for (transform in transforms) {
            placeholders += transform.generate()
        }

        return PlaceholderContext(placeholders, maxUpdateInterval)
    }

    /**
     * Registers one or more placeholders for a specified class type.
     *
     * @param T The type of the class for which placeholders are being registered.
     * @param clazz The class type associated with the placeholders.
     * @param placeholders The placeholders to be registered for the specified class.
     */
    fun <T> register(clazz: Class<T>, vararg placeholders: Placeholder<T>) {
        registered.getOrPut(clazz) { mutableListOf() }.addAll(placeholders)
    }

    inline fun <reified T> register(vararg placeholders: Placeholder<T>) {
        register(T::class.java, *placeholders)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getRegisteredFor(type: Class<T>): ArrayList<Placeholder<T>> {
        val placeholders = ArrayList<Placeholder<T>>()
        for ((clazz, list) in registered) {
            if (type == clazz || clazz.isAssignableFrom(type)) {
                placeholders += list.map { it as Placeholder<T> }
            }
        }
        return placeholders
    }

    class Transform<A, B>(
        val transformToClass: Class<B>,
        val func: (A) -> B
    ) {

        companion object {
            inline operator fun <reified A, reified B> invoke(noinline func: (A) -> B) = Transform(B::class.java, func)
        }

        internal fun generate(): List<Placeholder<A>> {
            val generated = ArrayList<Placeholder<A>>()
            val originals = getRegisteredFor(transformToClass)

            for (placeholder in originals) {
                generated.add(Placeholder(placeholder.identifier) { a, str ->
                    placeholder.apply(func(a), str)
                })
            }
            return generated
        }
    }
}