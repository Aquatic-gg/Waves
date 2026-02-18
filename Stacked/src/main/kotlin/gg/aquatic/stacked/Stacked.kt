package gg.aquatic.stacked

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.initializeCommon
import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

object Stacked {

    lateinit var scope: CoroutineScope
    lateinit var miniMessage: MiniMessage
    lateinit var bootstrapHolder: BootstrapHolder
}

fun initializeStacked(
    plugin: JavaPlugin,
    bootstrapHolder: BootstrapHolder,
    scope: CoroutineScope,
    miniMessage: MiniMessage = MiniMessage.miniMessage(),
    factories: Map<String, ItemFactory> = emptyMap()
) {
    Stacked.scope = scope
    Stacked.miniMessage = miniMessage
    Stacked.bootstrapHolder = bootstrapHolder

    try {
        val pl = AquaticCommon.plugin
    } catch (_: Exception) {
        initializeCommon(plugin)
    }

    StackedRegistryHolder.registryBootstrap(bootstrapHolder) {
        registry(ItemHandler.REGISTRY_KEY) {
            add("aquatic", ItemHandler.Impl)
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

