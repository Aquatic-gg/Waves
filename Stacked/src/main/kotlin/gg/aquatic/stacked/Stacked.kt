package gg.aquatic.stacked

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.MiniMessageResolver
import gg.aquatic.common.initializeCommon
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import kotlinx.coroutines.CoroutineScope
import org.bukkit.plugin.java.JavaPlugin

object Stacked {

    lateinit var scope: CoroutineScope
    lateinit var bootstrapHolder: BootstrapHolder
}

fun BootstrapHolder.initializeStacked(
    plugin: JavaPlugin,
    scope: CoroutineScope,
    miniMessage: MiniMessageResolver,
    factories: Map<String, ItemFactory> = emptyMap()
) {
    Stacked.scope = scope
    Stacked.bootstrapHolder = this

    try {
        val pl = AquaticCommon.plugin
    } catch (_: Exception) {
        initializeCommon(plugin, miniMessage)
    }

    StackedRegistryHolder.registryBootstrap(this) {
        registry(ItemHandler.REGISTRY_KEY) {
            add(ItemHandler.Impl.id, ItemHandler.Impl)
        }
        registry(StackedItem.ITEM_REGISTRY_KEY) {

        }
        registry(StackedItem.ITEM_FACTORY_REGISTRY_KEY) {
            for ((id, factory) in factories) {
                add(id, factory)
            }
        }
    }

    ItemManager.initialize()
}
