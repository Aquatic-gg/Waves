package gg.aquatic.waves.blockbenchfuture.blueprint

import gg.aquatic.waves.blockbenchfuture.raw.Float3
import gg.aquatic.waves.blockbenchfuture.raw.ModelElement
import gg.aquatic.waves.util.math.identifier
import org.joml.Quaternionf

interface BlueprintChildren {

    class Group(
        val name: String,
        private val _origin: Float3,
        val rotation: Float3,
        val children: List<BlueprintChildren>,
        val visibility: Boolean,
    ): BlueprintChildren {

        fun origin(): Float3 {
            return _origin.invertXZ()
        }

        fun scale(): Float {
            return children.filterIsInstance<Element>().maxOfOrNull { e -> e.element.max(_origin) / 16f } ?: 1.0f
        }

    }

    class Element(
        val element: ModelElement
    ): BlueprintChildren {

        companion object {
            fun centralize(target: Float3, groupOrigin: Float3, scale: Float): Float3 {
                return target.minus(groupOrigin).div(scale)
            }

            fun deltaPosition(target: Float3, quaternionf: Quaternionf): Float3 {
                return target.rotate(quaternionf).minus(target)
            }
        }

        fun identifier(): Float3 {
            return element.rotation().toVector().identifier().let { Float3(it.x, it.y, it.z) }
        }
    }

}