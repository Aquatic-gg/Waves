package gg.aquatic.waves.inventory

import gg.aquatic.waves.api.event.packet.PacketContainerClickEvent

class AccumulatedDrag(
    val packet: PacketContainerClickEvent,
    val type: ClickType
) {
}