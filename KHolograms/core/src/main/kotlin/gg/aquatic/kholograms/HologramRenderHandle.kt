package gg.aquatic.kholograms

import org.bukkit.Location
import org.bukkit.entity.Player

interface HologramRenderHandle {

    val entityIds: IntArray

    suspend fun move(location: Location, player: Player)

    fun destroy(player: Player)
}
