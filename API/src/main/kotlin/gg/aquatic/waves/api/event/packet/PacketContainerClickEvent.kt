package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PacketContainerClickEvent(
    val player: Player,
    var containerId: Int,
    var stateId: Int,
    var slotNum: Int,
    var buttonNum: Int,
    var clickTypeId: Int,
    var carriedItem: ItemStack?,
    var changedSlots: Map<Int, ItemStack?>
): PacketEvent()