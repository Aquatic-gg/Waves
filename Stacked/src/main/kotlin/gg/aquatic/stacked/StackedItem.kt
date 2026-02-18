package gg.aquatic.stacked

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.ItemOptions
import gg.aquatic.stacked.serialize.ItemSerializer
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface StackedItem<T : StackedItem<T>> {
    val options: MutableMap<Key, ItemOptionHandle>

    val handler: ItemHandler<T, out ItemSerializer<T>>

    fun getBaseItem(): ItemStack
    fun getItem(): ItemStack

    fun getOption(key: Key): ItemOptionHandle? {
        return options[key]
    }

    fun getOption(option: ItemOptions): ItemOptionHandle? {
        return options[option.key]
    }

    fun giveItem(player: Player, amount: Int? = null) {
        val iS = getItem()
        amount?.let { iS.amount = it }
        player.inventory.addItem(iS)
    }

    companion object {
        val ITEM_REGISTRY_KEY = RegistryKey.simple<String, StackedItem<*>>(
            RegistryId("aquatic", "items")
        )

        val ITEMS: Registry<String, StackedItem<*>>
            get() {
                return Stacked.bootstrapHolder[ITEM_REGISTRY_KEY]
            }

        val ITEM_FACTORY_REGISTRY_KEY = RegistryKey.simple<String, ItemFactory>(
            RegistryId("aquatic", "item_factories")
        )

        val ITEM_FACTORIES: Registry<String, ItemFactory>
            get() {
                return Stacked.bootstrapHolder[ITEM_FACTORY_REGISTRY_KEY]
            }

        @Suppress("UNCHECKED_CAST")
        fun <T : StackedItem<T>> serializer(id: String): ItemSerializer<T> = (ItemHandler.REGISTRY[id]?.serializer
            ?: throw IllegalArgumentException("No $id item handler found")) as ItemSerializer<T>
    }
}