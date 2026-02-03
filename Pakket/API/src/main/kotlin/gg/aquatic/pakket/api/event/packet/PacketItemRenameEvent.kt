package gg.aquatic.pakket.api.event.packet

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

class PacketItemRenameEvent(
    val player: Player,
    val name: String
): PacketEvent()