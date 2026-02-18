package gg.aquatic.statistik

import gg.aquatic.kregistry.bootstrap.BootstrapHolder

internal lateinit var bootstrapHolder: BootstrapHolder

fun initializeStatistik(bootstrapHolder: BootstrapHolder) {
    gg.aquatic.statistik.bootstrapHolder = bootstrapHolder

    StatistikRegistryHolder.registryBootstrap(bootstrapHolder) {
        registry(StatisticType.REGISTRY_KEY) {

        }
    }
}