package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

data class PacketDestroyEntitiesPacket(
    val player: Player,
    val entityIds: IntArray
): PacketEvent()