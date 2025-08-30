package gg.aquatic.waves.scenario.prop

import gg.aquatic.waves.scenario.prop.path.PathBoundProperties
import gg.aquatic.waves.scenario.prop.path.PathPoint
import gg.aquatic.waves.scenario.prop.path.PathProp
import gg.aquatic.waves.scenario.Scenario
import org.bukkit.Location
import org.bukkit.util.Vector
import org.joml.Math.toRadians
import java.util.concurrent.ConcurrentHashMap

interface Moveable {
    val locationOffset: Vector
    val locationOffsetYawPitch: Pair<Float, Float>
    val scenario: Scenario

    val boundPaths: ConcurrentHashMap<PathProp, Pair<PathBoundProperties, Int>>
    val processedPaths: MutableSet<PathProp>

    //val boundLocationOffset: Vector?

    fun processPath(
        path: PathProp,
        point: PathPoint
    ) {

        processedPaths += path
        if (processedPaths.size != boundPaths.size) {
            return
        }

        val currentPoint = calculatePoint()

        val newLocation = scenario.baseLocation.clone().add(currentPoint.vector).add(locationOffset)
        newLocation.yaw = currentPoint.yaw + locationOffsetYawPitch.first
        newLocation.pitch = currentPoint.pitch + locationOffsetYawPitch.second

        move(newLocation)
        processedPaths.clear()
    }

    fun calculatePoint(): PathPoint {
        var currentPoint = PathPoint(0.0, 0.0, 0.0, 0f, 0f)
        for ((p, pair) in boundPaths.entries.sortedBy { it.value.second }) {
            val (properties, _) = pair
            val po = p.currentPoint
            val offset = properties.offset

            val pointVector = Vector(po.x, po.y, po.z)
            /*
                .rotateAroundY(-toRadians(currentPoint.yaw.toDouble()))
                .rotateAroundX(-toRadians(currentPoint.pitch.toDouble()))

             */

            var yaw: Float
            var pitch: Float

            if (properties.affectYawPitch) {
                yaw = currentPoint.yaw + po.yaw
                pitch = currentPoint.pitch + po.pitch
            } else {
                yaw = currentPoint.yaw
                pitch = currentPoint.pitch
            }
            yaw += offset.yaw
            pitch += offset.pitch

            if (properties.offsetType == PathBoundProperties.OffsetType.STATIC) {
                currentPoint = PathPoint(
                    pointVector.x + offset.x + currentPoint.x,
                    pointVector.y + offset.y + currentPoint.y,
                    pointVector.z + offset.z + currentPoint.z,
                    yaw,
                    pitch
                )
            } else {
                val newV = pointVector.clone().add(
                    offset.vector
                        .rotateAroundY(-toRadians(po.yaw).toDouble())
                        .rotateAroundX(-toRadians(po.pitch).toDouble())
                )
                val x = newV.x + currentPoint.x
                val y = newV.y + currentPoint.y
                val z = newV.z + currentPoint.z
                currentPoint = PathPoint(x, y, z, yaw, pitch)
            }
        }
        return currentPoint
    }

    fun move(location: Location)

}