package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.core.SimpleRegistry
import gg.aquatic.kregistry.core.SimpleRegistryKey
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistry
import gg.aquatic.kregistry.grouped.GroupedRegistryKey

/**
 * Interface representing a holder for bootstrapping registry-related operations.
 * A `BootstrapHolder` is responsible for managing the lifecycle of registries, including
 * injection into the bootstrap system, rebuilding registries, and ensuring proper
 * initialization and updates of registry data.
 */
interface BootstrapHolder {
    /**
     * Injects the current instance of `BootstrapHolder` into the `RegistryBootstrap` system and sets
     * it as the active bootstrap holder. This method ensures that the `BootstrapHolder` is registered
     * for managing registry contributions and initialization.
     *
     * The returned lambda function, when invoked, triggers the building of all registries managed by the
     * `RegistryBootstrap`. This includes processing registry definitions and initializing the
     * registry graph.
     *
     * @return A lambda function that builds all registries when executed.
     */
    fun inject(): () -> Unit {
        val bootstrap = RegistryBootstrap()
        BootstrapHolderState.set(this, bootstrap)

        return {
            bootstrap.buildRegistries()
        }
    }

    /**
     * Rebuilds all registries associated with the specified holder. This method iterates
     * through the current registries, identifies those utilizing contributions from the given holder,
     * rebuilds those registries, and updates the registry graph accordingly.
     *
     * @param holder The `RegistryHolder` whose associated registries are to be rebuilt.
     */
    fun rebuildRegistries(holder: RegistryHolder) {
        bootstrapOrThrow().rebuildRegistries(holder)
    }

    /**
     * Refreshes the provided `registry` by rebuilding its data and incorporating
     * any updated contributions associated with the specified `holder`.
     *
     * @param A The type of the keys in the registry.
     * @param B The type of the values in the registry.
     * @param registry The registry instance to be refreshed. This encompasses the current
     *                 set of key-value pairs and any holder-specific contributions.
     * @param holder The holder whose contributions will be updated within the registry.
     *               This is used to rebuild parts of the registry data if relevant
     *               updates are available.
     */
    fun <A, B> refreshRegistry(registry: Registry<A, B>, holder: RegistryHolder) {
        bootstrapOrThrow().refreshRegistry(registry, holder)
    }

    /**
     * Returns a registry by key from the current graph snapshot.
     */
    fun <K, V> registry(key: RegistryKey<K, V>): Registry<K, V> = bootstrapOrThrow().getRegistry(key)

    operator fun <K, V> get(key: RegistryKey<K, V>): Registry<K, V> = registry(key)

    fun <K, V> get(key: SimpleRegistryKey<K, V>): SimpleRegistry<K, V> =
        registry(key) as SimpleRegistry<K, V>

    operator fun <Id, Group, Value : GroupedEntry<Group>> get(
        key: GroupedRegistryKey<Id, Group, Value>
    ): GroupedRegistry<Id, Group, Value> =
        registry(key) as GroupedRegistry<Id, Group, Value>

    private fun bootstrapOrThrow(): RegistryBootstrap =
        BootstrapHolderState.get(this)
}
