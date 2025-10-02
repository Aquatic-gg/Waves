package gg.aquatic.waves.blockbench.timeline

import org.bukkit.util.Vector

class TimelineUtil {

    companion object {
        fun lerp(lower: Vector, higher: Vector, progress: Double): Vector {
            return if (lower == higher) {
                lower
            } else Vector(
                lerp(lower.x, higher.x, progress),
                lerp(lower.y, higher.y, progress),
                lerp(lower.z, higher.z, progress)
            )
        }

        fun lerp(d1: Double, d2: Double, d3: Double): Double {
            return (1.0 - d3) * d1 + d3 * d2
        }

        fun lerp(d1: Double, d2: Double, d3: Double, d4: Double): Double {
            return d3 * d1 + d4 * d2
        }

        fun lerp(v1: Vector, v2: Vector, d1: Double, d2: Double): Vector {
            return Vector(
                lerp(v1.x, v2.x, d1, d2),
                lerp(v1.y, v2.y, d1, d2),
                lerp(v1.z, v2.z, d1, d2)
            )
        }

        fun smoothLerp(v1: Vector, v2: Vector, v3: Vector, v4: Vector, dd: Double): Vector {
            val d1 = 0.0
            val d2 = 1.0
            val d3 = 2.0
            val d4 = 3.0

            var d = dd
            d = (d3 - d2) * d + d2
            val vector1 = lerp(v1, v2, (d2 - d) / (d2 - d1), (d - d1) / (d2 - d1))
            val vector2 = lerp(v2, v3, (d3 - d) / (d3 - d2), (d - d2) / (d3 - d2))
            val vector3 = lerp(v3, v4, (d4 - d) / (d4 - d3), (d - d3) / (d4 - d3))
            val vector4 = lerp(vector1, vector2, (d3 - d) / (d3 - d1), (d - d1) / (d3 - d1))
            val vector5 = lerp(vector2, vector3, (d4 - d) / (d4 - d2), (d - d2) / (d4 - d2))
            return lerp(vector4, vector5, (d3 - d) / (d3 - d2), (d - d2) / (d3 - d2))
        }
    }

}