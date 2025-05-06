package gg.aquatic.waves.api.nms

import org.bukkit.entity.Player

class PacketEntity(
    val entityId: Int,
    val entityInstance: Any,
    var spawnPacket: Any,
    var updatePacket: Any? = null,
    var passengerPacket: Any? = null
) {

    fun sendSpawnComplete(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        sendSpawn(nmsHandler, silent, *players)
        sendDataUpdate(nmsHandler, silent, *players)
        sendPassengerUpdate(nmsHandler, silent, *players)
    }

    fun sendSpawn(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        nmsHandler.sendPacket(spawnPacket,silent, *players)
    }
    fun sendDataUpdate(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        updatePacket?.let {
            nmsHandler.sendPacket(it, silent, *players)
        }
    }
    fun sendPassengerUpdate(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        passengerPacket?.let {
            nmsHandler.sendPacket(it, silent, *players)
        }
    }
}