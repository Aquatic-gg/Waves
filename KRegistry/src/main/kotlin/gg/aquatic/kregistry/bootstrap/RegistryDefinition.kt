package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.core.SimpleRegistry
import gg.aquatic.kregistry.core.SimpleRegistryKey
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistry
import gg.aquatic.kregistry.grouped.GroupedRegistryKey

class RegistryDefinition<A, B>(
    val id: RegistryKey<A, B>,
) {
    val builders = hashMapOf<RegistryHolder, RegistryContributionBuilder<A, B>.() -> Unit>()

    internal fun build(): Registry<A, B> {
        val data = hashMapOf<A, B>()
        val holderData = hashMapOf<RegistryHolder, Map<A, B>>()
        for ((holder, builder) in builders) {
            val builderInst = RegistryContributionBuilder<A, B>()
            builder(builderInst)
            holderData[holder] = builderInst.data
            for ((key, value) in builderInst.data) {
                val existing = data[key]
                if (existing is Registry<*, *> && value is Registry<*, *>) {
                    data[key] = mergeRegistry(existing, value) as B
                } else {
                    data[key] = value
                }
            }
        }
        return createRegistry(id, data, holderData)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <K, V> createRegistry(
        key: RegistryKey<K, V>,
        data: Map<K, V>,
        holderData: Map<RegistryHolder, Map<K, V>>
    ): Registry<K, V> {
        return when (key) {
            is GroupedRegistryKey<*, *, *> -> {
                @Suppress("UNCHECKED_CAST")
                val groupedKey = key as GroupedRegistryKey<Any, Any, GroupedEntry<Any>>
                @Suppress("UNCHECKED_CAST")
                val groupedData = data as Map<Class<out Any>, Registry<Any, GroupedEntry<Any>>>
                @Suppress("UNCHECKED_CAST")
                val groupedHolder = holderData as Map<RegistryHolder, Map<Class<out Any>, Registry<Any, GroupedEntry<Any>>>>
                GroupedRegistry(groupedKey, groupedData, groupedHolder) as Registry<K, V>
            }
            is SimpleRegistryKey<*, *> -> SimpleRegistry(
                key as SimpleRegistryKey<Any, Any>,
                data as Map<Any, Any>,
                holderData as Map<RegistryHolder, Map<Any, Any>>
            ) as Registry<K, V>
            else -> SimpleRegistry(
                key as SimpleRegistryKey<Any, Any>,
                data as Map<Any, Any>,
                holderData as Map<RegistryHolder, Map<Any, Any>>
            ) as Registry<K, V>
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeRegistry(existing: Registry<*, *>, incoming: Registry<*, *>): Registry<*, *> {
        require(existing.registryKey == incoming.registryKey) {
            "Cannot merge registries with different keys: ${existing.registryKey.id} vs ${incoming.registryKey.id}"
        }

        val mergedData = HashMap<Any, Any>()
        mergedData.putAll(existing.data as Map<Any, Any>)
        mergedData.putAll(incoming.data as Map<Any, Any>)

        val mergedHolderData = HashMap<RegistryHolder, Map<Any, Any>>()
        mergedHolderData.putAll(existing.holderData as Map<RegistryHolder, Map<Any, Any>>)
        mergedHolderData.putAll(incoming.holderData as Map<RegistryHolder, Map<Any, Any>>)

        val registry = existing as Registry<Any, Any>
        return registry.withData(mergedData, mergedHolderData)
    }
}
