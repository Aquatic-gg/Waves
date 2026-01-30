package gg.aquatic.stacked

import gg.aquatic.kevent.eventBusBuilder
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import gg.aquatic.stacked.event.StackedItemInteractEvent
import gg.aquatic.stacked.impl.StackedItemImpl
import gg.aquatic.stacked.option.ItemOptionHandle
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

abstract class ItemHandler<T : StackedItem<T>> {

    lateinit var id: String
        internal set

    companion object {
        val NAMESPACE_KEY by lazy {
            NamespacedKey("Stacked", "Custom_Item_Registry")
        }

        val REGISTRY_KEY = RegistryKey<String, ItemHandler<*>>(RegistryId("aquatic", "item_handlers"))
        val REGISTRY: FrozenRegistry<String, ItemHandler<*>>
            get() {
                return Registry[REGISTRY_KEY]
            }
    }

    val serializer = ItemSerializer(this)

    val listenInteractions = mutableMapOf<String, (StackedItemInteractEvent) -> Unit>()

    val eventBus by lazy {
        eventBusBuilder {
            scope = Stacked.scope
            hierarchical = false
        }
    }

    abstract fun create(
        item: ItemStack,
        options: List<ItemOptionHandle>
    ): T

    object Impl : ItemHandler<StackedItemImpl>() {
        override fun create(
            item: ItemStack,
            options: List<ItemOptionHandle>
        ): StackedItemImpl {
            return StackedItemImpl(item, options, this)
        }
    }
}