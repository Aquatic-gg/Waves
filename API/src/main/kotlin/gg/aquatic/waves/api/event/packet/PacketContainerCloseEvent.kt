package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import org.bukkit.entity.Player

class PacketContainerCloseEvent(
    val player: Player
): CancellableAquaticEvent(true) {
}