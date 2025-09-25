package gg.aquatic.waves.blockbench.animation

import gg.aquatic.waves.util.math.toQuaternionf
import org.joml.Quaternionf
import org.joml.Vector3f

class BoneMovement(
    val transform: Vector3f,
    val scale: Vector3f,
    val rotation: Quaternionf,
    val rawRotation: Vector3f,
) {

    fun plus(movement: AnimationMovement): BoneMovement {
        val mov = movement.position
        val scl = movement.scale
        val rot = movement.rotation
        val rawRot = rot?.let { Vector3f(it).add(rawRotation) } ?: Vector3f(rawRotation)
        return BoneMovement(
            mov?.let { Vector3f(transform).add(it) } ?: Vector3f(transform),
            scl?.let { Vector3f(scale).mul(Vector3f(it).add(1f, 1f, 1f)) } ?: Vector3f(scale),
            rot?.let { rawRot.toQuaternionf() } ?: Quaternionf(rotation),
            rawRot,
        )
    }
}