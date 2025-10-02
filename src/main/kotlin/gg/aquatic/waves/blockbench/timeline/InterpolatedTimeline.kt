package gg.aquatic.waves.blockbench.timeline

import org.bukkit.util.Vector
import java.util.*


class InterpolatedTimeline<T: InterpolatedKeyframe>(
    timeline: TreeMap<Double, T>
) : Timeline<T>(timeline) {

    constructor() : this(TreeMap())

    fun interpolatedValue(time: Double): Vector {
        if (timeline.isEmpty()) {
            return Vector(0, 0, 0)
        }
        val lowerD = getLower(time, timeline)
        val higherD: Double = getHigher(time, timeline)
        val lower = timeline[lowerD]!!
        val higher = timeline[higherD]!!
        if (lowerD == higherD) {
            return lower.vector
        }
        val interpolation = getInterpolation(lower, higher)

        // Progress
        val d = (time - lowerD) / (higherD - lowerD)
        when (interpolation) {
            InterpolationType.LINEAR -> {
                return TimelineUtil.lerp(lower.vector, higher.vector, d)
            }
            InterpolationType.SMOOTH -> {
                val lowerLowerD = getLower(lowerD, timeline)
                val higherHigherD: Double = getHigher(higherD, timeline)
                val lowerLower = timeline[lowerLowerD]
                val higherHigher = timeline[higherHigherD]
                return TimelineUtil.smoothLerp(
                    lowerLower!!.vector,
                    lower.vector,
                    higher.vector,
                    higherHigher!!.vector,
                    d
                )
            }
            InterpolationType.STEP -> {
                return lower.vector
            }
            else -> return Vector()
        }
    }

    private fun getLower(value: Double, map: TreeMap<Double, *>): Double {
        return map.lowerKey(value) ?: return map.firstKey()
    }

    private fun getHigher(value: Double, map: TreeMap<Double, *>): Double {
        return map.higherKey(value) ?: return map.lastKey()
    }

    private fun getInterpolation(keyFrame1: InterpolatedKeyframe, keyFrame2: InterpolatedKeyframe): InterpolationType {
        if (keyFrame1.interpolationType == InterpolationType.STEP) {
            return InterpolationType.STEP
        }
        return if (keyFrame1.interpolationType == InterpolationType.SMOOTH || keyFrame2.interpolationType == InterpolationType.SMOOTH) {
            InterpolationType.SMOOTH
        } else InterpolationType.LINEAR
    }

}