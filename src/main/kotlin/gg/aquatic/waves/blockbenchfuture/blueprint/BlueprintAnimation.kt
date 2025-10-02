package gg.aquatic.waves.blockbenchfuture.blueprint

import gg.aquatic.waves.blockbenchfuture.animation.AnimationIterator
import gg.aquatic.waves.blockbenchfuture.animation.AnimationMovement

class BlueprintAnimation(
    val name: String,
    val loop: AnimationIterator.Type,
    val length: Float,
    val override: Boolean,
    val animator: Map<String, BlueprintAnimator>,
    val emptyAnimator: List<AnimationMovement>
) {

    fun emptyIterator(type: AnimationIterator.Type) = type.create(
        emptyAnimator
    )
}