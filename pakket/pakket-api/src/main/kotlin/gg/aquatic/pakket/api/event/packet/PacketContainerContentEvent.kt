package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PacketContainerContentEvent(
    val player: Player,
    val inventoryId: Int,
    val contents: MutableList<ItemStack>,
    var carriedItem: ItemStack
): PacketEvent()