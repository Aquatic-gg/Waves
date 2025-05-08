package gg.aquatic.waves.api.nms

import org.bukkit.Location
import org.bukkit.entity.Player

class PacketEntity(
    location: Location,
    val entityId: Int,
    val entityInstance: Any,
    var spawnPacket: Any,
    var updatePacket: Any? = null,
    var passengerPacket: Any? = null,
    var equipmentPacket: Any? = null,
    val despawnpacket: Any
) {

    var location: Location = location
        private set

    fun sendSpawnComplete(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        sendSpawn(nmsHandler, silent, *players)
        sendDataUpdate(nmsHandler, silent, *players)
        sendPassengerUpdate(nmsHandler, silent, *players)
        sendEquipmentUpdate(nmsHandler, silent, *players)
    }

    fun sendSpawn(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        nmsHandler.sendPacket(spawnPacket, silent, *players)
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

    fun sendEquipmentUpdate(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        equipmentPacket?.let {
            nmsHandler.sendPacket(it, silent, *players)
        }
    }

    fun sendDespawn(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        nmsHandler.sendPacket(despawnpacket, silent, *players)
    }

    fun teleport(nmsHandler: NMSHandler, location: Location, silent: Boolean = false, vararg players: Player) {
        setLocation(nmsHandler, location)
        val packet = nmsHandler.createTeleportEntityPacket(this, location)
        nmsHandler.sendPacket(packet, silent, *players)
    }

    fun setLocation(nmsHandler: NMSHandler, location: Location) {
        this.location = location
        val recreatedPacket = nmsHandler.recreateEntityPacket(this,location)
        spawnPacket = recreatedPacket
    }
}