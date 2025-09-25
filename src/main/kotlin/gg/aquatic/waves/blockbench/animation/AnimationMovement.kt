package gg.aquatic.waves.blockbench.animation

import gg.aquatic.waves.blockbench.interpolation.Timed
import org.joml.Vector3f

class AnimationMovement(
    override val time: Float,
    val position: Vector3f? = null,
    val scale: Vector3f? = null,
    val rotation: Vector3f? = null,
) : Timed {

    companion object {
        val EMPTY = AnimationMovement(0f)

        fun withEmpty(time: Float) = listOf(EMPTY, AnimationMovement(time))
    }

    fun empty(): AnimationMovement {
        if (!hasKeyframe()) return this
        return if (time < 0f) EMPTY else AnimationMovement(time)
    }

    fun hasKeyframe(): Boolean {
        return position != null || scale != null || rotation != null
    }

    fun time(newTime: Float) = if (newTime == time) this else AnimationMovement(
        newTime,
        position,
        scale,
        rotation
    )
}