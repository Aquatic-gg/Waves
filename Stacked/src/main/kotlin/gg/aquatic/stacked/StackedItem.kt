package gg.aquatic.stacked

import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.ItemOptions
import gg.aquatic.stacked.serialize.ItemSerializer
import gg.aquatic.stacked.serialize.ItemSerializerImpl
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
        val ITEM_REGISTRY_KEY = RegistryKey<String, StackedItem<*>>(
            RegistryId("aquatic", "items")
        )

        val ITEMS: FrozenRegistry<String, StackedItem<*>>
            get() {
                return Registry[ITEM_REGISTRY_KEY]
            }

        val ITEM_FACTORY_REGISTRY_KEY = RegistryKey<String, ItemFactory>(
            RegistryId("aquatic", "item_factories")
        )

        val ITEM_FACTORIES: FrozenRegistry<String, ItemFactory>
            get() {
                return Registry[ITEM_FACTORY_REGISTRY_KEY]
            }

        @Suppress("UNCHECKED_CAST")
        fun <T : StackedItem<T>> serializer(id: String): ItemSerializer<T> = (ItemHandler.REGISTRY[id]?.serializer
            ?: throw IllegalArgumentException("No $id item handler found")) as ItemSerializer<T>
    }
}