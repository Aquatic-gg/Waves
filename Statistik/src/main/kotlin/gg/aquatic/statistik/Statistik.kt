package gg.aquatic.statistik

import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.MutableRegistry
import gg.aquatic.kregistry.Registry

fun initializeStatistik() {
    val registry = MutableRegistry<Class<*>, FrozenRegistry<String, StatisticType<*>>>()

    Registry.update { registerRegistry(StatisticType.Companion.REGISTRY_KEY, registry.freeze()) }
}