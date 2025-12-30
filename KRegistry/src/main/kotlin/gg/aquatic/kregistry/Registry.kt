package gg.aquatic.kregistry

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
object Registry {

    private val graphRef =
        AtomicReference(
            MutableRegistryGraph().freeze()
        )

    /**
     * Retrieves the current immutable graph representation of all registries.
     *
     * @return The current `FrozenRegistryGraph` instance containing all registries in their frozen state.
     */
    fun graph(): FrozenRegistryGraph = graphRef.load()

    /**
     * Retrieves the frozen registry associated with the specified registry key.
     *
     * @param key The registry key to look up. Contains a unique identifier for the desired registry.
     * @return The frozen registry corresponding to the provided key.
     * @throws IllegalStateException if the registry associated with the key is not found.
     */
    operator fun <K, V> get(key: RegistryKey<K, V>): FrozenRegistry<K, V> =
        graph().getRegistry(key)

    /**
     * Updates the current registry graph by applying the provided updater function,
     * ensuring thread-safe modifications through atomic compare-and-set operations.
     *
     * @param updater A function that takes a `MutableRegistryGraph` as input,
     * allowing for custom modifications to the graph. The changes made within
     * this function are atomically applied to the registry graph to maintain consistency.
     */
    fun update(
        updater: MutableRegistryGraph.() -> Unit
    ) {
        val current = graphRef.load()
        val mutable = current.unfreeze()

        updater(mutable)

        val updated = mutable.freeze()
        if (graphRef.compareAndSet(current, updated)) {
            return
        }
    }
}