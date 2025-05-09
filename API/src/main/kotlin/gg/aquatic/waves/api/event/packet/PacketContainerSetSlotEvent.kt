package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketContainerSetSlotEvent(
    val player: Player,
    val inventoryId: Int,
    val slot: Int,
    var item: ItemStack
): CancellableAquaticEvent(true) {
}