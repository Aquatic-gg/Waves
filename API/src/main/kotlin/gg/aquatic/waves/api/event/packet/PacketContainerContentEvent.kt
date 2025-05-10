package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketContainerContentEvent(
    val player: Player,
    val inventoryId: Int,
    val contents: MutableList<ItemStack>,
    var carriedItem: ItemStack
): PacketEvent() {
}