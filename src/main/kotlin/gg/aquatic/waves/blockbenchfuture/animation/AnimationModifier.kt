package gg.aquatic.waves.blockbenchfuture.animation

import org.bukkit.entity.Player
import java.util.function.BooleanSupplier

class AnimationModifier(
    val predicate: BooleanSupplier?,
    val start: Int,
    val end: Int,
    val type: AnimationIterator.Type?,
    val speed: (()->Float)?,
    val override: Boolean?,
    val player: Player?
) {

    companion object {
        val DEFAULT = AnimationModifier(null, 1, 0, null, null, null, null)
    }

    fun speedValue(): Float {
        return speed?.invoke() ?: 1f
    }

    fun overrideValue(original: Boolean): Boolean {
        return override ?: original
    }

    fun predicateValue(): Boolean {
        return predicate == null || predicate.asBoolean
    }

    fun type(defaultType: AnimationIterator.Type): AnimationIterator.Type = type ?: defaultType
}