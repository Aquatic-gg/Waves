package gg.aquatic.kregistry

class FrozenRegistryGraph internal constructor(
    private val registries: Map<RegistryId, FrozenRegistry<*, *>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <K, V> getRegistry(key: RegistryKey<K, V>): FrozenRegistry<K, V> =
        registries[key.id] as? FrozenRegistry<K, V>
            ?: error("Registry not found: ${key.id}")

    fun unfreeze(): MutableRegistryGraph =
        MutableRegistryGraph(registries.toMutableMap())
}