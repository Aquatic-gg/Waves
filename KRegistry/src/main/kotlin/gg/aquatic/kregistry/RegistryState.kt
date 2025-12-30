package gg.aquatic.kregistry

sealed interface RegistryState<K, V> {
    operator fun get(key: K): V?

    fun getOrThrow(key: K): V =
        get(key) ?: error("Missing entry: $key")

    fun getAll(): Map<K, V>
}