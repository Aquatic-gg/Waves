package gg.aquatic.waves.api

class PacketEntity(
    val entityId: Int,
    val entityInstance: Any,
    var spawnPacket: Any,
    var updatePacket: Any? = null
) {
}