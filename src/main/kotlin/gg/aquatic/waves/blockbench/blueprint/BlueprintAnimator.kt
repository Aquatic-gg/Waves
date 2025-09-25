package gg.aquatic.waves.blockbench.blueprint

import gg.aquatic.waves.blockbench.animation.AnimationIterator
import gg.aquatic.waves.blockbench.animation.AnimationMovement
import gg.aquatic.waves.blockbench.interpolation.VectorPoint

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