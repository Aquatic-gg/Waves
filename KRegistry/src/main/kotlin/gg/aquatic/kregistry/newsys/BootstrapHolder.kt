package gg.aquatic.kregistry.newsys

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
        RegistryBootstrap.injectBootstrapHolder(this)

        return {
            RegistryBootstrap.buildRegistries()
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
        RegistryBootstrap.rebuildRegistries(holder)
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
        RegistryBootstrap.refreshRegistry(registry, holder)
    }
}