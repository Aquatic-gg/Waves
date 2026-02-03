package gg.aquatic.kmenu.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/** Tracks per-player menu viewer state. */
class InventoryViewer(
    val player: Player,
    var carriedItem: ItemStack? = null
) {

    internal val accumulatedDrag: MutableList<AccumulatedDrag> = mutableListOf()

    fun changeCarriedItem(itemStack: ItemStack?) {
        carriedItem = itemStack
        player.openInventory.setCursor(itemStack)
    }
}
