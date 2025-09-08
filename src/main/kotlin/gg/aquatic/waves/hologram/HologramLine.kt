package gg.aquatic.waves.hologram

import gg.aquatic.waves.api.nms.PacketEntity
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import org.bukkit.Location
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.Player

interface HologramLine {

    var scale: Float
    var billboard: Billboard
    var transformationDuration: Int
    var teleportInterpolation: Int
    var translation: org.joml.Vector3f

    val height: Double
    val filter: (Player) -> Boolean

    val failLine: HologramLine?

    fun getVisibleLine(player: Player): HologramLine? =
        if (filter(player)) {
            this
        } else {
            failLine?.getVisibleLine(player)
        }

    fun spawn(location: Location, player: Player, textUpdater: (String) -> String): PacketEntity
    fun tick(spawnedHologramLine: SpawnedHologramLine)
    fun buildData(textUpdater: (String) -> String): List<EntityDataValue>

    fun buildData(spawnedHologramLine: SpawnedHologramLine): List<EntityDataValue> {
        return buildData { str ->
            spawnedHologramLine.textUpdater(spawnedHologramLine.player, str)
        }
    }
}