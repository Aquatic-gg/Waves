package gg.aquatic.statistik

import gg.aquatic.kregistry.bootstrap.BootstrapHolder

internal lateinit var bootstrapHolder: BootstrapHolder

fun BootstrapHolder.initializeStatistik(statistics: Map<String, StatisticType<*>>) {
    bootstrapHolder = this

    StatistikRegistryHolder.registryBootstrap(this) {
        registry(StatisticType.REGISTRY_KEY) {
            statistics.forEach { (key, value) -> add(key, value) }
        }
    }
}