package gg.aquatic.clientside

import gg.aquatic.clientside.serialize.*
import gg.aquatic.kregistry.bootstrap.BootstrapHolder

fun BootstrapHolder.initializeClientside() {
    FakeObjectHandler.bootstrapHolder = this

    ClientsideRegistryHolder.registryBootstrap(this) {
        registry(ClientsideSettings.Factory.REGISTRY_KEY) {
            add("block", ClientsideBlockSettings.Companion)
            add("entity", ClientsideEntitySettings.Companion)
            add("meg", ClientsideMEGSettings.Companion)
            add("multiblock", ClientsideMultiBlockSettings.Companion)
        }
    }

    FakeObjectHandler.initialize()
}