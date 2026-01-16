package gg.aquatic.replace

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Placeholders {

    val registered by lazy {
        val map = hashMapOf<Class<*>, MutableCollection<Placeholder<*>>>()

        // Safe check for server existence to prevent NPE during Unit Tests
        val server = try { Bukkit.getServer() } catch (_: Throwable) { null }
        val isPapiInstalled = server?.pluginManager?.getPlugin("PlaceholderAPI") != null

        if (isPapiInstalled) {
            map += Player::class.java to mutableListOf(Placeholder.PAPIPlaceholder)
        }

        map
    }

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
        val placeholders = getRegisteredFor(clazz).toMutableList()
        transforms.forEach { placeholders += it.generate() }

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

    inline fun <reified T> registerDSL(
        identifier: String,
        isConst: Boolean = false,
        noinline block: PlaceholderDSLNode<T>.() -> Unit
    ) {
        register(dslPlaceholder(identifier, isConst, block))
    }

    inline fun <reified T> register(vararg placeholders: Placeholder<T>) {
        register(T::class.java, *placeholders)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getRegisteredFor(type: Class<T>): List<Placeholder<T>> {
        return registered.entries
            .filter { it.key.isAssignableFrom(type) || type == it.key }
            .flatMap { it.value }
            .map { it as Placeholder<T> }
    }

    class Transform<A, B>(
        val transformToClass: Class<B>,
        val func: (A) -> B
    ) {
        companion object {
            inline operator fun <reified A, reified B> invoke(noinline func: (A) -> B) =
                Transform(B::class.java, func)
        }

        internal fun generate(): List<Placeholder<A>> {
            return getRegisteredFor(transformToClass).map { it.map(func) }
        }
    }
}