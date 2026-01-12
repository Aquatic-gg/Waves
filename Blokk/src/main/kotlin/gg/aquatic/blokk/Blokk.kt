package gg.aquatic.blokk

import gg.aquatic.blokk.factory.BlockFactory
import gg.aquatic.kregistry.MutableRegistry
import gg.aquatic.kregistry.Registry
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

abstract class Blokk {

    abstract fun place(location: Location)
    abstract val blockData: BlockData

}

fun initializeBlokk(factories: Map<String, BlockFactory>) {
    val registry = MutableRegistry<String, BlockFactory>()
    factories.forEach { (key, factory) -> registry.register(key, factory) }
    Registry.update {
        registerRegistry(BlockFactory.REGISTRY_KEY, registry.freeze())
    }
}