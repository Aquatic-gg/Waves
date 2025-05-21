package gg.aquatic.waves.api.nms.meg

import com.ticxo.modelengine.api.nms.network.ProtectedPacket

object MEGPacketHandler {

    fun isMegPacket(packet: Any): Boolean {
        return packet is ProtectedPacket
    }

    fun unpackPacket(packet: Any): Any {
        val protectedPacket = packet as ProtectedPacket
        return protectedPacket.packet
    }
}