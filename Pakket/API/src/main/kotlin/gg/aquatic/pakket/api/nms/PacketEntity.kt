package gg.aquatic.pakket.api.nms

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class PacketEntity(
    location: Location,
    val entityId: Int,
    val entityInstance: Any,
    var spawnPacket: Any,
    var updatePacket: Any? = null,
    var passengerPacket: Any? = null,
    val despawnpacket: Any,
    var seatPacket: Any? = null,
) {
    val equipment = HashMap<EquipmentSlot, ItemStack?>()

    var location: Location = location
        private set

    fun bukkitEntity(nmsHandler: NMSHandler): Entity {
        return nmsHandler.getBukkitEntity(this)
    }

    fun sendSpawnComplete(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        val packets = ArrayList<Any>()
        packets += spawnPacket
        updatePacket?.let { packets += it }
        passengerPacket?.let { packets += it }
        seatPacket?.let { packets += it }

        val bundlePacket = nmsHandler.createBundlePacket(packets)
        for (player in players) {
            nmsHandler.sendPacket(bundlePacket, silent, player)
        }
        sendEquipmentUpdate(nmsHandler, *players)
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

    fun sendSeatUpdate(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        seatPacket?.let {
            nmsHandler.sendPacket(it, silent, *players)
        }
    }

    fun sendEquipmentUpdate(nmsHandler: NMSHandler, vararg players: Player) {
        if (equipment.isEmpty()) {
            return
        }
        for (player in players) {
            player.sendEquipmentChange(
                bukkitEntity(nmsHandler) as? LivingEntity ?: return,
                equipment.mapValues { it.value ?: ItemStack.empty() })
        }
    }

    fun sendDespawn(nmsHandler: NMSHandler, silent: Boolean = false, vararg players: Player) {
        nmsHandler.sendPacket(despawnpacket, silent, *players)
    }

    fun teleport(nmsHandler: NMSHandler, location: Location, silent: Boolean = false, vararg players: Player) {
        setLocation(nmsHandler, location)
        val packet = nmsHandler.createTeleportPacket(entityId, location)
        nmsHandler.sendPacket(packet, silent, *players)
    }

    fun setLocation(nmsHandler: NMSHandler, location: Location) {
        this.location = location
        val recreatedPacket = nmsHandler.recreateEntityPacket(this, location)
        spawnPacket = recreatedPacket
    }
}