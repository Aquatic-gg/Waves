package gg.aquatic.kholograms

import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.sendPacket
import org.bukkit.Location
import org.bukkit.entity.Player

class PacketEntityHologramRenderHandle(
    val packetEntity: PacketEntity,
) : HologramRenderHandle {

    override val entityIds: IntArray
        get() = intArrayOf(packetEntity.entityId)

    override suspend fun move(location: Location, player: Player) {
        packetEntity.teleport(Pakket.handler, location, false, player)
    }

    fun sendSpawn(player: Player) {
        packetEntity.sendSpawnComplete(Pakket.handler, false, player)
    }

    fun update(data: List<EntityDataValue>, player: Player) {
        if (data.isEmpty()) return
        val packet = Pakket.handler.createEntityUpdatePacket(packetEntity.entityId, data)
        packetEntity.updatePacket = packet
        player.sendPacket(packet, false)
    }

    override fun destroy(player: Player) {
        packetEntity.sendDespawn(Pakket.handler, false, player)
    }
}
