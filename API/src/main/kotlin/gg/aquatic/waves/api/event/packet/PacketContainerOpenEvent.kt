package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.CancellableAquaticEvent
import gg.aquatic.waves.api.event.PacketEvent
import org.bukkit.entity.Player

class PacketContainerOpenEvent(
    val player: Player,
    val containerId: Int,
): PacketEvent()