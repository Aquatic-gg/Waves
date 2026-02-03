package gg.aquatic.kholograms

import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.replace.PlaceholderContext
import org.bukkit.Location
import org.bukkit.entity.Player

class HologramLineHandle(
    val hologram: Hologram,
    val player: Player,
    val line: HologramLine,
    location: Location,
    val placeholderContext: PlaceholderContext<Player>,
    var packetEntity: PacketEntity
) {

    init {
        packetEntity.sendSpawnComplete(Pakket.handler, false, player)
    }

    var currentLocation: Location = location
        private set

    suspend fun tick() {
        line.tick(this)
    }

    fun move(location: Location) {
        currentLocation = location
        packetEntity.teleport(Pakket.handler, location, false, player)
    }

    fun destroy() {
        packetEntity.sendDespawn(Pakket.handler, false, player)
    }
}