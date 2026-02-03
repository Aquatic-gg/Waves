package gg.aquatic.pakket.api.nms.listener

import org.bukkit.entity.Player

fun interface OutgoingPacketHandler {
    fun handle(packet: Any, player: Player): OutgoingHandlerResult?
}