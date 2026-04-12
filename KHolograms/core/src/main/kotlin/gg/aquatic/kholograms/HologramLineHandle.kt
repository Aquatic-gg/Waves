package gg.aquatic.kholograms

import gg.aquatic.replace.PlaceholderContext
import org.bukkit.Location
import org.bukkit.entity.Player

class HologramLineHandle(
    val hologram: Hologram,
    val player: Player,
    location: Location,
    val placeholderContext: PlaceholderContext<Player>,
    var renderHandle: HologramRenderHandle,
    var renderedLine: HologramLine,
    var sourceIndex: Int
) {

    var currentLocation: Location = location
        private set

    suspend fun move(location: Location) {
        currentLocation = location
        renderHandle.move(location, player)
    }

    fun destroy() {
        renderHandle.destroy(player)
    }
}
