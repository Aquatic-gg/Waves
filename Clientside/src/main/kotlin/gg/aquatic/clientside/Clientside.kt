package gg.aquatic.clientside

import gg.aquatic.clientside.serialize.ClientsideBlockSettings
import gg.aquatic.clientside.serialize.ClientsideEntitySettings
import gg.aquatic.clientside.serialize.ClientsideMEGSettings
import gg.aquatic.clientside.serialize.ClientsideMultiBlockSettings
import gg.aquatic.clientside.serialize.ClientsideSettings
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