package gg.aquatic.waves.blockbenchfuture.animation

import gg.aquatic.waves.blockbenchfuture.interpolation.Interpolation
import gg.aquatic.waves.blockbenchfuture.interpolation.Interpolation.FLOAT_COMPARISON_EPSILON
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.roundToInt

class RenderedGroup(
    val updateConsumer: (Int, Vector3f, Quaternionf, Vector3f) -> Unit,
    val parent: RenderedGroup?,
    val defaultFrame: BoneMovement,
) {
    var scale: () -> Float = { 1f }
    var positionModifier: (Vector3f) -> Vector3f = { v -> v }
    var rotationModifier: (Quaternionf) -> Quaternionf = { q -> q }

    private var lastModifiedPosition = Vector3f()
    private var lastModifiedRotation = Quaternionf()

    val state = GroupStateHandler(
    )

    private fun modifiedPosition(prevent: Boolean): Vector3f {
        return if (prevent) lastModifiedPosition else {
            lastModifiedPosition = positionModifier(Vector3f())
            lastModifiedPosition
        }
    }

    private fun modifiedRotation(prevent: Boolean): Quaternionf {
        return if (prevent) lastModifiedRotation else {
            lastModifiedRotation = rotationModifier(Quaternionf())
            lastModifiedRotation
        }
    }

    /*
    fun realPosition(): Vector3f {
        val progress = state.progress()
        val after = state.afterTransform ?: state.relativeOffset()
        val before = state.beforeTranform ?: BoneMovement(Vector3f(), Vector3f(1f), Quaternionf(), Vector3f())
        return Interpolation.fma(
            Interpolation.lerp(before.transform,after.transform, progress)
                .rotate(Interpolation.lerp(before.rawRotation,after.rawRotation, progress).toQuaternionf()),
            Interpolation.lerp(before.scale,after.scale,progress),
            Vector3f()
        ).mul(scale())
    }
     */

    inner class GroupStateHandler(

    ) {

        @Volatile
        var beforeTranform: BoneMovement? = null

        @Volatile
        var afterTransform: BoneMovement? = null

        @Volatile
        var relativeOffsetCache: BoneMovement? = null

        val state = AnimationStateHandler(
            AnimationMovement.EMPTY,
            { a, s, t -> a.time(t) },
            { b, a -> relativeOffsetCache = null },
        )

        private var firstTick = true

        fun tick(): Boolean {
            val result = state.tick { } || firstTick
            if (result) {
                sendUpdate()
            }
            firstTick = false
            return result
        }

        fun progress(): Float {
            return 1f - state.progress()
        }

        fun defaultFrame(): BoneMovement {
            val keyframe = state.afterKeyframe
            return defaultFrame.plus(keyframe ?: AnimationMovement.EMPTY)
        }

        fun interpolationDuration(): Int {
            val frame = state.frame() / 5f
            return (frame + FLOAT_COMPARISON_EPSILON).roundToInt()
        }

        fun nextMovement(): BoneMovement {
            beforeTranform = afterTransform
            afterTransform = relativeOffset()
            return afterTransform!!
        }

        fun relativeOffset(): BoneMovement {
            if (relativeOffsetCache != null) return relativeOffsetCache!!
            val def = defaultFrame()
            val preventModifierUpdate = interpolationDuration() < 1
            if (parent != null) {
                val p = parent.state.relativeOffset()
                relativeOffsetCache = BoneMovement(
                    Interpolation.fma(
                        def.transform.rotate(p.rotation),
                        p.scale,
                        p.transform
                    ).sub(parent.lastModifiedPosition)
                        .add(modifiedPosition(preventModifierUpdate)),
                    def.scale.mul(p.scale),
                    p.rotation.div(parent.lastModifiedRotation, Quaternionf())
                        .mul(def.rotation)
                        .mul(modifiedRotation(preventModifierUpdate)),
                    def.rawRotation
                )
                return relativeOffsetCache!!
            }
            relativeOffsetCache = BoneMovement(
                def.transform.add(modifiedPosition(preventModifierUpdate)),
                def.scale,
                def.rotation.mul(modifiedRotation(preventModifierUpdate)),
                def.rawRotation
            )
            return relativeOffsetCache!!
        }

        private fun sendUpdate() {
            val movement = nextMovement()
            val mul = scale()

            updateConsumer(
                interpolationDuration(),
                movement.transform.mul(mul),
                movement.rotation,
                movement.scale.mul(mul).max(Vector3f())
            )
        }
    }
}

