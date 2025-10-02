package gg.aquatic.waves.blockbenchfuture.blueprint

import gg.aquatic.waves.blockbenchfuture.animation.AnimationIterator
import gg.aquatic.waves.blockbenchfuture.animation.AnimationMovement
import gg.aquatic.waves.blockbenchfuture.interpolation.VectorPoint

class BlueprintAnimator(
    val name: String,
    val keyFrame: List<AnimationMovement>
) {

    class AnimatorData(
        val name: String,
        val position: List<VectorPoint>,
        val rotation: List<VectorPoint>,
        val scale: List<VectorPoint>
    ) {

        fun allPoints() = listOf(position, rotation, scale).flatten()
    }

    fun iterator(type: AnimationIterator.Type): AnimationIterator<AnimationMovement> {
        return type.create(keyFrame)
    }
}