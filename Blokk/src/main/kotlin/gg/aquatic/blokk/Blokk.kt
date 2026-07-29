package gg.aquatic.blokk

import gg.aquatic.blokk.factory.BlockFactory
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import org.bukkit.Location
import org.bukkit.block.data.BlockData

abstract class Blokk {

    abstract fun place(location: Location)
    abstract val blockData: BlockData
    open fun blockDataAt(location: Location): BlockData = blockData

}

internal lateinit var bootstrapHolder: BootstrapHolder

fun initializeBlokk(bootstrapHolder: BootstrapHolder, factories: Map<String, BlockFactory>) {
    gg.aquatic.blokk.bootstrapHolder = bootstrapHolder
    BlokkRegistryHolder.registryBootstrap(bootstrapHolder) {
        registry(BlockFactory.REGISTRY_KEY) {
            for ((key, factory) in factories) {
                add(key, factory)
            }
        }
    }
}
