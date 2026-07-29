package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

data class PacketChunkLoadEvent(
    val player: Player,
    val x: Int,
    val z: Int,
    val packet: Any,
    val extraPackets: MutableList<Any>
): PacketEvent()