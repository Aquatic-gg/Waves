package gg.aquatic.waves.hologram

import org.bukkit.entity.Display.Billboard

class CommonHologramLineSettings(
    var scale: Float,
    var billboard: Billboard,
    var transformationDuration: Int,
    var teleportInterpolation: Int,
    val height: Double
) {
}