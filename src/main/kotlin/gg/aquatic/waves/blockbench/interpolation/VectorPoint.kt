package gg.aquatic.waves.blockbench.interpolation

import org.joml.Vector3f

class VectorPoint(
    val function: (Float) -> Vector3f,
    override val time: Float,
    val interpolator: VectorInterpolation
): Timed {

    companion object {
        val EMPTY = VectorPoint(
            { Vector3f() },
            0f,
            VectorInterpolation.Linear()
        )
    }

    fun vector(time: Float): Vector3f {
        return function(time)
    }

    fun vector(): Vector3f {
        return vector(this.time)
    }

}