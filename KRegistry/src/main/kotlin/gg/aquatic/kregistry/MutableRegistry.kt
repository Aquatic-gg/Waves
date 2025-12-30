package gg.aquatic.kregistry

open class MutableRegistry<K, V> protected constructor(
    protected val entries: MutableMap<K, V>
) : RegistryState<K, V> {

    constructor() : this(mutableMapOf())

    fun register(key: K, value: V) {
        require(key !in entries)
        entries[key] = value
    }

    fun unregister(key: K): V? {
        return entries.remove(key)
    }

    fun clear() {
        entries.clear()
    }

    fun freeze(): FrozenRegistry<K, V> =
        FrozenRegistry(entries.toMap())

    override fun get(key: K): V =
        entries[key] ?: error("Missing entry: $key")

    override fun getAll(): Map<K, V> {
        return entries
    }

    internal companion object {
        internal fun <K, V> from(entries: Map<K, V>): MutableRegistry<K, V> =
            MutableRegistry(entries.toMutableMap())
    }
}