package gg.aquatic.stacked.factory

import gg.aquatic.stacked.ItemFactory
import gg.aquatic.stacked.ItemHandler
import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.inventory.ItemStack

object OraxenFactory: ItemFactory {
    override fun create(id: String): ItemStack? {
        val item = OraxenItems.getItemById(id)?.build()
        if (item == null) {
            println("Failed to create oraxen item for $id. No Oraxen item with this ID found!")
        }
        return item
    }
}