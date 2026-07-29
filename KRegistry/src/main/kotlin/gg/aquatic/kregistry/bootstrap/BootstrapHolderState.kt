package gg.aquatic.kregistry.bootstrap

internal object BootstrapHolderState {
    private val bootstraps = java.util.IdentityHashMap<BootstrapHolder, RegistryBootstrap>()

    fun set(holder: BootstrapHolder, bootstrap: RegistryBootstrap) {
        bootstraps[holder] = bootstrap
    }

    fun get(holder: BootstrapHolder): RegistryBootstrap =
        bootstraps[holder] ?: error("BootstrapHolder not injected")
}
