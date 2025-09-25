package gg.aquatic.waves.blockbench.raw

import gg.aquatic.waves.blockbench.animation.AnimationIterator
import gg.aquatic.waves.blockbench.blueprint.BlueprintChildren

data class ModelAnimation(
    val name: String,
    val loop: AnimationIterator.Type?,
    val override: Boolean,
    val uuid: String,
    val animators: Map<String,ModelAnimator>?
) {

    fun loop(): AnimationIterator.Type {
        return loop ?: AnimationIterator.Type.PLAY_ONCE
    }
    fun animators(): Map<String,ModelAnimator> {
        return animators ?: emptyMap()
    }

    fun toBlueprint(children: List<BlueprintChildren>) {
        val map = animators().toMutableMap()
    }
}