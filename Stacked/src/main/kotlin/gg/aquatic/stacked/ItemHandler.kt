package gg.aquatic.stacked

import gg.aquatic.kevent.suspendingEventBusBuilder
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.stacked.event.StackedItemInteractEvent
import gg.aquatic.stacked.impl.StackedItemImpl
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.serialize.ItemSerializer
import gg.aquatic.stacked.serialize.ItemSerializerImpl
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

abstract class ItemHandler<T : StackedItem<T>, S: ItemSerializer<T>> {

    lateinit var id: String
        internal set

    companion object {
        val NAMESPACE_KEY by lazy {
            NamespacedKey("stacked", "custom_item_registry")
        }

        val REGISTRY_KEY = RegistryKey.simple<String, ItemHandler<*,*>>(RegistryId("aquatic", "item_handlers"))
        val REGISTRY: Registry<String, ItemHandler<*,*>>
            get() {
                return Stacked.bootstrapHolder[REGISTRY_KEY]
            }
    }

    abstract val serializer: ItemSerializer<T>

    val listenInteractions = ConcurrentHashMap<String, (StackedItemInteractEvent) -> Unit>()

    val eventBus by lazy {
        suspendingEventBusBuilder {
            scope = Stacked.scope
            hierarchical = false
        }
    }

    object Impl : ItemHandler<StackedItemImpl, ItemSerializerImpl>() {
        fun create(
            item: ItemStack,
            options: List<ItemOptionHandle>
        ): StackedItemImpl {
            return StackedItemImpl(item, options, this)
        }

        override val serializer: ItemSerializerImpl = ItemSerializerImpl
    }
}