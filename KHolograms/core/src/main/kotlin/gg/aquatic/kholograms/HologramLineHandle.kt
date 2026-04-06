package gg.aquatic.kholograms

import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.PacketEntity
import gg.aquatic.replace.PlaceholderContext
import org.bukkit.Location
import org.bukkit.entity.Player

class HologramLineHandle(
    val hologram: Hologram,
    val player: Player,
    location: Location,
    val placeholderContext: PlaceholderContext<Player>,
    var packetEntity: PacketEntity,
    var renderedLine: HologramLine,
    var sourceIndex: Int
) {

    init {
        packetEntity.sendSpawnComplete(Pakket.handler, false, player)
    }

    var currentLocation: Location = location
        private set

    fun move(location: Location) {
        currentLocation = location
        packetEntity.teleport(Pakket.handler, location, false, player)
    }

    fun destroy() {
        packetEntity.sendDespawn(Pakket.handler, false, player)
    }
}
