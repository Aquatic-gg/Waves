package gg.aquatic.kregistry

import kotlin.collections.mutableMapOf

class MutableRegistryGraph internal constructor(
    private val registries: MutableMap<RegistryId, FrozenRegistry<*, *>>
) {

    constructor() : this(mutableMapOf())

    fun <K, V> registerRegistry(
        key: RegistryKey<K, V>,
        registry: FrozenRegistry<K, V>
    ) {
        require(key.id !in registries) {
            "Registry already exists: ${key.id}"
        }
        registries[key.id] = registry
    }

    fun <K, V> replaceRegistry(
        key: RegistryKey<K, V>,
        registry: FrozenRegistry<K, V>
    ) {
        require(key.id in registries) {
            "Registry does not exist: ${key.id}"
        }
        registries[key.id] = registry
    }


    fun <K, V> replaceRegistry(
        key: RegistryKey<K, V>,
        update: MutableRegistry<K, V>.() -> Unit
    ) {
        require(key.id in registries) {
            "Registry does not exist: ${key.id}"
        }
        registries[key.id] = getRegistry(key).updateRegistry(update)
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, V> getRegistry(
        key: RegistryKey<K, V>
    ): FrozenRegistry<K, V> =
        registries[key.id] as? FrozenRegistry<K, V>
            ?: error("Registry not found: ${key.id}")

    fun freeze(): FrozenRegistryGraph =
        FrozenRegistryGraph(registries.toMap())
}