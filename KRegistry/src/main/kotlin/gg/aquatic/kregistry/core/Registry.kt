package gg.aquatic.kregistry.core

import gg.aquatic.kregistry.bootstrap.RegistryHolder

interface Registry<A, B> {
    val registryKey: RegistryKey<A, B>
    val data: Map<A, B>
    val holderData: Map<RegistryHolder, Map<A, B>>

    operator fun get(key: A): B? = data[key]
    fun all(): Map<A, B> = data
    fun withData(data: Map<A, B>, holderData: Map<RegistryHolder, Map<A, B>>): Registry<A, B>
}

class SimpleRegistry<A, B>(
    override val registryKey: RegistryKey<A, B>,
    override val data: Map<A, B>,
    override val holderData: Map<RegistryHolder, Map<A, B>>
) : Registry<A, B> {
    override fun withData(
        data: Map<A, B>,
        holderData: Map<RegistryHolder, Map<A, B>>
    ): Registry<A, B> = SimpleRegistry(registryKey, data, holderData)
}
