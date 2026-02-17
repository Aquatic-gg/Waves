package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.RegistryKey

internal class Contribution(
    val holder: RegistryHolder,
    val builder: Map<RegistryKey<*, *>, RegistryContributionBuilder<*, *>.() -> Unit>
)

class ContributionBuilder {
    private val data = HashMap<RegistryKey<*, *>, RegistryContributionBuilder<*, *>.() -> Unit>()

    @Suppress("UNCHECKED_CAST")
    fun <A, B> registry(key: RegistryKey<A, B>, builder: RegistryContributionBuilder<A, B>.() -> Unit) {
        data[key] = builder as RegistryContributionBuilder<*, *>.() -> Unit
    }

    internal fun build(holder: RegistryHolder): Contribution {
        return Contribution(holder, data)
    }
}

class RegistryContributionBuilder<A, B> {

    internal val data = HashMap<A, B>()

    fun add(key: A, value: B) {
        data[key] = value
    }
}
