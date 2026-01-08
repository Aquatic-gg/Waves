package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class PacketContainerClickEvent(
    val player: Player,
    var containerId: Int,
    var stateId: Int,
    var slotNum: Int,
    var buttonNum: Int,
    var clickTypeId: Int,
    var carriedItem: ItemStack?,
    var changedSlots: Map<Int, ItemStack?>
): PacketEvent()