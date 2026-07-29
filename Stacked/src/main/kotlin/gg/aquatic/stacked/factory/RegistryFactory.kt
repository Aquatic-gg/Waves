package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.StackedItem
import org.bukkit.inventory.ItemStack

object RegistryFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        return StackedItem.ITEMS[id]?.getItem()
    }
}