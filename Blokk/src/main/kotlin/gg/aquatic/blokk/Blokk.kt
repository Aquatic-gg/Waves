package gg.aquatic.blokk

import gg.aquatic.blokk.factory.BlockFactory
import gg.aquatic.kregistry.MutableRegistry
import gg.aquatic.kregistry.Registry

fun initializeBlokk(factories: Map<String, BlockFactory>) {
    val registry = MutableRegistry<String, BlockFactory>()
    factories.forEach { (key, factory) -> registry.register(key, factory) }
    Registry.update {
        registerRegistry(BlockFactory.REGISTRY_KEY, registry.freeze())
    }
}