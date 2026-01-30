package gg.aquatic.stacked

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.initializeCommon
import gg.aquatic.kregistry.MutableRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.stacked.factory.Base64Factory
import gg.aquatic.stacked.factory.RegistryFactory
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin

object Stacked {

    lateinit var scope: CoroutineScope
    lateinit var miniMessage: MiniMessage

    fun injectFactories(customFactories: Map<String, ItemFactory> = emptyMap()) {
        Registry.update {
            replaceRegistry(StackedItem.ITEM_FACTORY_REGISTRY_KEY) {
                injectFactories(this, customFactories)
            }
        }
    }

    fun injectFactories(
        registry: MutableRegistry<String, ItemFactory>,
        customFactories: Map<String, ItemFactory> = emptyMap()
    ) {
        registry.register("registry", RegistryFactory)
        registry.register("base64", Base64Factory)
        customFactories.forEach { (key, factory) -> registry.register(key, factory) }
    }
}

fun initializeStacked(plugin: JavaPlugin, scope: CoroutineScope, miniMessage: MiniMessage = MiniMessage.miniMessage()) {
    Stacked.scope = scope
    Stacked.miniMessage = miniMessage

    try {
        val pl = AquaticCommon.plugin
    } catch (_: Exception) {
        initializeCommon(plugin)
    }

    val registry = MutableRegistry<String, ItemHandler<*,*>>()
    Registry.update {
        registerRegistry(ItemHandler.REGISTRY_KEY, registry.freeze())
    }

    ItemManager.injectHandler("aquatic", ItemHandler.Impl)
    ItemManager.initialize()
}

