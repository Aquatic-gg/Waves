package gg.aquatic.pakket.api.nms.listener

import gg.aquatic.pakket.api.event.PacketEvent

sealed class OutgoingHandlerResult {
    data class Forward(val event: PacketEvent?, val packetFactory: () -> Any) : OutgoingHandlerResult()
    data class Drop(val event: PacketEvent? = null) : OutgoingHandlerResult()
}