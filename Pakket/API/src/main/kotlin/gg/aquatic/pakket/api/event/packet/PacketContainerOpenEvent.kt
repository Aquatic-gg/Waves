package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

data class PacketContainerOpenEvent(
    val player: Player,
    val containerId: Int,
): PacketEvent()