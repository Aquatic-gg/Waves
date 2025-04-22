package gg.aquatic.waves.inventory

import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class InventoryViewer(
    val player: Player,
    var carriedItem: com.github.retrooper.packetevents.protocol.item.ItemStack? = null,
    val accumulatedDrag: MutableList<AccumulatedDrag> = mutableListOf()
) {

    fun changeCarriedItem(itemStack: ItemStack?) {
        carriedItem = itemStack?.let { SpigotConversionUtil.fromBukkitItemStack(it) }
        player.openInventory.setCursor(itemStack)
    }
}