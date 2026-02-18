package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryKey

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
        return Registry(id, data, holderData)
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

        return Registry(
            existing.registryKey as RegistryKey<Any, Any>,
            mergedData,
            mergedHolderData
        )
    }
}
