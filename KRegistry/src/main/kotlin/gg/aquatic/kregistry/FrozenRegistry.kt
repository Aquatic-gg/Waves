package gg.aquatic.kregistry

open class FrozenRegistry<K, V>(
    protected val entries: Map<K, V>
) : RegistryState<K, V> {

    override operator fun get(key: K): V? = entries[key]
    override fun getAll(): Map<K, V> {
        return entries
    }

    open fun unfreeze(): MutableRegistry<K, V> =
        MutableRegistry.from(entries.toMutableMap())

    open fun updateRegistry(apply: MutableRegistry<K, V>.() -> Unit) = unfreeze().apply(apply).freeze()
}