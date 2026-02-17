package gg.aquatic.kregistry

class Registry<A, B>(
    val registryKey: RegistryKey<A, B>,
    internal val data: Map<A, B>,
    internal val holderData: Map<RegistryHolder, Map<A, B>>
) {

    fun get(key: A): B? {
        return data[key]
    }

    fun all(): Map<A, B> = data
}
