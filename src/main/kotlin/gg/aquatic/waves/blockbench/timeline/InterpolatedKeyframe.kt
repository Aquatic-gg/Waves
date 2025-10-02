package gg.aquatic.waves.blockbench.timeline

import org.bukkit.util.Vector

abstract class InterpolatedKeyframe(
    val vector: Vector
): Keyframe {

    var interpolatedVector: Vector = vector
    var interpolationType: InterpolationType = InterpolationType.LINEAR
}