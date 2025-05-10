package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import org.bukkit.entity.Player

class PacketChunkLoadEvent(
    val player: Player,
    val x: Int,
    val z: Int,
    val packet: Any,
    val extraPackets: MutableList<Any>
): CancellableAquaticEvent(true) {
}