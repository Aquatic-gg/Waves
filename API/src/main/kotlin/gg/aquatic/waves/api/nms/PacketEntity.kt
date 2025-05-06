package gg.aquatic.waves.api.nms

class PacketEntity(
    val entityId: Int,
    val entityInstance: Any,
    var spawnPacket: Any,
    var updatePacket: Any? = null
) {
}