package gg.aquatic.kregistry.newsys

class RegistryDefinition<A, B>(
    val id: RegistryKey<A, B>,
) {
    val builders = hashMapOf<RegistryHolder, RegistryContributionBuilder<A, B>.() -> Unit>()

    internal fun build(): Registry<A, B> {
        val data = hashMapOf<A, B>()
        val holderData = hashMapOf<RegistryHolder, Map<A, B>>()
        for ((holder, builder) in builders) {
            val builderInst = RegistryContributionBuilder<A, B>()
            builder(RegistryContributionBuilder())
            holderData[holder] = builderInst.data
            data.putAll(builderInst.data)
        }
        return Registry(id, data, holderData)
    }
}