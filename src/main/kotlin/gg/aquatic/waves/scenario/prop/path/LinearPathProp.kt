package gg.aquatic.waves.scenario.prop.path

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.scenario.prop.Moveable
import java.util.TreeMap
import kotlin.collections.iterator

class LinearPathProp(
    override val points: TreeMap<Int, PathPoint>,
    override val scenario: Scenario
): ScenarioProp, PathProp {
    override var currentPoint: PathPoint = PathPoint(0.0, 0.0, 0.0, 0f, 0f)
        private set

    override val boundProps: MutableMap<Moveable, PathBoundProperties> = HashMap()

    var tick = 0

    init {
        tick()
    }

    override fun tick() {
        if (points.isEmpty()) {
            for ((prop, _) in boundProps) {
                prop.processPath(this@LinearPathProp, PathPoint(0.0, 0.0, 0.0, 0f, 0f))
            }
            return
        }
        if (tick > points.lastKey()) {
            val lastPoint = points.lastEntry().value
            for ((prop, _) in boundProps) {
                prop.processPath(this@LinearPathProp, lastPoint)
            }
            return
        }

        val lowerPoint = lowerPoint()
        if (lowerPoint == null) {
            tick++
            for ((prop, _) in boundProps) {
                prop.processPath(this@LinearPathProp, PathPoint(0.0, 0.0, 0.0, 0f, 0f))
            }
            return
        }
        if (lowerPoint.second == points.lastEntry().value) {
            for ((prop, _) in boundProps) {
                prop.processPath(this@LinearPathProp, lowerPoint.second)
            }
            return
        }
        val upperPoint = points.higherEntry(tick).toPair()

        if (upperPoint.second == lowerPoint.second) {
            for ((prop, _) in boundProps) {
                prop.processPath(this@LinearPathProp, upperPoint.second)
            }
            return
        }

        val duration = upperPoint.first - lowerPoint.first
        val currentTick = tick - lowerPoint.first

        val ratio = currentTick.toDouble() / duration.toDouble()

        val interpolatedX = interpolate(lowerPoint.second.x, upperPoint.second.x, ratio)
        val interpolatedY = interpolate(lowerPoint.second.y, upperPoint.second.y, ratio)
        val interpolatedZ = interpolate(lowerPoint.second.z, upperPoint.second.z, ratio)
        val interpolatedYaw = interpolate(lowerPoint.second.yaw.toDouble(), upperPoint.second.yaw.toDouble(), ratio).toFloat()
        val interpolatedPitch = interpolate(lowerPoint.second.pitch.toDouble(), upperPoint.second.pitch.toDouble(), ratio).toFloat()

        val point = PathPoint(interpolatedX, interpolatedY, interpolatedZ, interpolatedYaw, interpolatedPitch)
        currentPoint = point

        for ((prop, _) in boundProps) {
            prop.processPath(this@LinearPathProp, point)
        }


        /*
        for (boundProp in boundProps) {
            boundProp.move(location.clone().add(boundProp.boundLocationOffset ?: Vector()))
        }
         */
        tick++

    }

    override fun onEnd() {
        tick = 0
    }

    private fun lowerPoint(): Pair<Int, PathPoint>? {
        val point = points[tick]
        if (point != null) {
            return Pair(tick, point)
        }
        return points.lowerEntry(tick)?.toPair()
    }

    private fun interpolate(start: Double, end: Double, ratio: Double): Double {
        return start + (end - start) * ratio
    }
}