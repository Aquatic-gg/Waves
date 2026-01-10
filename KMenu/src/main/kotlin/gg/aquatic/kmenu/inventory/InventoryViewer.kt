package gg.aquatic.kmenu.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class InventoryViewer(
    val player: Player,
    var carriedItem: ItemStack? = null,
    val accumulatedDrag: MutableList<AccumulatedDrag> = mutableListOf()
) {

    fun changeCarriedItem(itemStack: ItemStack?) {
        carriedItem = itemStack
        player.openInventory.setCursor(itemStack)
    }
}