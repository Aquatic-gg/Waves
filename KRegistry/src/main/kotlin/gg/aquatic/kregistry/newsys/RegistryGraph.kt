package gg.aquatic.kregistry.newsys


class RegistryGraph internal constructor(
    internal val registries: Map<RegistryKey<*,*>, Registry<*, *>>
) {

    @Suppress("UNCHECKED_CAST")
    fun <K, V> getRegistry(key: RegistryKey<K, V>): Registry<K, V> =
        registries[key] as? Registry<K, V>
            ?: error("Registry not found: ${key.id}")

    internal fun rebuild(registry: Registry<*,*>): RegistryGraph {
        val snapshot = registries.toMutableMap()
        snapshot[registry.registryKey] = registry
        return RegistryGraph(snapshot)
    }

    internal fun rebuild(registries: Collection<Registry<*, *>>): RegistryGraph {
        val snapshot = this.registries.toMutableMap()
        registries.forEach { snapshot[it.registryKey] = it }
        return RegistryGraph(snapshot)
    }
}