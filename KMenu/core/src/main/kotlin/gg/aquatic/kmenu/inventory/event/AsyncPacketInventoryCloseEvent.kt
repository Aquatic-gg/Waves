package gg.aquatic.kmenu.inventory.event

import gg.aquatic.kmenu.inventory.PacketInventory
import org.bukkit.entity.Player

/** Fired when a packet menu is closed. */
class AsyncPacketInventoryCloseEvent(
    val player: Player,
    val inventory: PacketInventory
)
