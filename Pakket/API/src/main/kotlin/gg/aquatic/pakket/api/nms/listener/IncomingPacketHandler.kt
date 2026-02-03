package gg.aquatic.pakket.api.nms.listener

import gg.aquatic.pakket.api.event.PacketEvent
import org.bukkit.entity.Player

fun interface IncomingPacketHandler {
    fun handle(packet: Any, player: Player): PacketEvent?
}