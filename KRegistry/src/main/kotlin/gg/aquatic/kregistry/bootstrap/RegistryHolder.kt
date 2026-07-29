package gg.aquatic.kregistry.bootstrap

/**
 * Represents an entity capable of holding and configuring registry contributions.
 *
 * The `RegistryHolder` interface provides a mechanism to bootstrap registry contributions
 * through a structured DSL (Domain-Specific Language) using the provided `ContributionBuilder`.
 * Implementers of this interface can define their specific registry contributions, which are
 * then injected into the global `RegistryBootstrap`.
 *
 * ## Responsibilities:
 * - Acts as a contract for defining registry contributions.
 * - Provides a method to configure and build registry contributions through a DSL.
 * - Ensures the constructed contributions are injected into the registry system.
 *
 * ## Key Method:
 * - `registryBootstrap(builder: ContributionBuilder.() -> Unit)`:
 *   Allows defining registry contributions using a lambda with receiver on a `ContributionBuilder`.
 *   The contributions built through the `ContributionBuilder` are then registered in the system.
 */
interface RegistryHolder {

    /**
     * Bootstraps and registers a registry contribution into the global `RegistryBootstrap` system.
     * This method allows clients to define registry contributions using a DSL provided by
     * the `ContributionBuilder`. Once defined, the contribution is built and injected into
     * the global registry system.
     *
     * @param builder A lambda with receiver that defines the registry contribution.
     *                The receiver is an instance of `ContributionBuilder` where
     *                contributions can be configured.
     */
    fun registryBootstrap(bootstrap: BootstrapHolder, builder: ContributionBuilder.() -> Unit) {
        val contrBuilder = ContributionBuilder()
        contrBuilder.builder()
        val contribution = contrBuilder.build(this)
        BootstrapHolderState.get(bootstrap).injectContribution(contribution)
    }
}
