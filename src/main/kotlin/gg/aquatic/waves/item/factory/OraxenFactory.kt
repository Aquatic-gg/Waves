package gg.aquatic.waves.item.factory

import gg.aquatic.waves.item.ItemHandler
import io.th0rgal.oraxen.api.OraxenItems
import org.bukkit.inventory.ItemStack

object OraxenFactory: ItemHandler.Factory {
    override fun create(id: String): ItemStack? {
        val item = OraxenItems.getItemById(id)?.build()
        if (item == null) {
            println("Failed to create oraxen item for $id. No Oraxen item with this ID found!")
        }
        return item
    }
}