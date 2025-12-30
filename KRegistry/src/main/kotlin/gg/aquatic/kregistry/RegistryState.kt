package gg.aquatic.kregistry

sealed interface RegistryState<K, V> {
    operator fun get(key: K): V?

    fun getAll(): Map<K, V>
}