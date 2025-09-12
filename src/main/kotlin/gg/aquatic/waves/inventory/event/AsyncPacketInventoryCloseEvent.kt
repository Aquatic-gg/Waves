package gg.aquatic.waves.inventory.event

import gg.aquatic.waves.inventory.PacketInventory
import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.entity.Player

class AsyncPacketInventoryCloseEvent(
    val player: Player,
    val inventory: PacketInventory
): AquaticEvent(true)