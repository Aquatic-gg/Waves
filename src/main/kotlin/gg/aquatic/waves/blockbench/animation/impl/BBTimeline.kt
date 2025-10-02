package gg.aquatic.waves.blockbench.animation.impl

import gg.aquatic.waves.blockbench.timeline.InterpolatedTimeline
import org.bukkit.util.Vector

class BBTimeline(
    val positionTimeline: InterpolatedTimeline<PositionKeyframe>,
    val rotationTimeline: InterpolatedTimeline<RotationKeyframe>
) {

    fun getPositionFrame(time: Double): Vector {
        return positionTimeline.interpolatedValue(time)
    }

    fun getRotationFrame(time: Double): Vector {
        return rotationTimeline.interpolatedValue(time)
    }

}