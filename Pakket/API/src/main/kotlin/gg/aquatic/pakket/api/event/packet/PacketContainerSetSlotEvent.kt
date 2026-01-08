package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PacketContainerSetSlotEvent(
    val player: Player,
    val inventoryId: Int,
    val slot: Int,
    var item: ItemStack
): PacketEvent()