package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemHandler
import gg.aquatic.stacked.StackedItem
import org.bukkit.inventory.ItemStack

object RegistryFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        return StackedItem.ITEMS[id]?.getItem()
    }
}