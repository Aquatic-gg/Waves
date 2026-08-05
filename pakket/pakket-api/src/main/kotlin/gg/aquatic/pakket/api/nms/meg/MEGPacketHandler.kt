package gg.aquatic.pakket.api.nms.meg

import com.ticxo.modelengine.api.nms.network.ProtectedPacket

object MEGPacketHandler {

    fun isMegPacket(packet: Any): Boolean = packet is ProtectedPacket

    fun unpackPacket(packet: Any): Any {
        return (packet as? ProtectedPacket)?.packet ?: packet
    }
}