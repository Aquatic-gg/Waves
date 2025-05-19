package gg.aquatic.waves.api.event.packet

import gg.aquatic.waves.api.event.PacketEvent

class PacketDestroyEntitiesPacket(
    val entityIds: IntArray
): PacketEvent() {
}