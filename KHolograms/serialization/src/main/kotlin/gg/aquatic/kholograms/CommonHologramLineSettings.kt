package gg.aquatic.kholograms

import org.bukkit.entity.Display.Billboard
import org.joml.Vector3f

class CommonHologramLineSettings(
    var scale: Float,
    var billboard: Billboard,
    var transformationDuration: Int,
    var teleportInterpolation: Int,
    val height: Double,
    val translation: Vector3f
)