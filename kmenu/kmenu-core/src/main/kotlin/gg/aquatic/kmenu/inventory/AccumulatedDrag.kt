package gg.aquatic.kmenu.inventory

import gg.aquatic.pakket.api.event.packet.PacketContainerClickEvent

internal class AccumulatedDrag(
    val packet: PacketContainerClickEvent,
    val type: ClickType
)
