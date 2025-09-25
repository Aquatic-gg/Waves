package gg.aquatic.waves.blockbench.animation

import gg.aquatic.waves.blockbench.animation.AnimationStateHandler.MappingState
import gg.aquatic.waves.blockbench.interpolation.Interpolation.FRAME_EPSILON
import gg.aquatic.waves.blockbench.interpolation.Timed
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BooleanSupplier
import kotlin.math.roundToInt

class AnimationStateHandler<T : Timed>(
    val initialValue: T,
    val mapper: AnimationMapper<T>,
    val setConsumer: (T, T) -> Unit,
) {

    var delay: Int = 0
    val animators: SequencedMap<String, TreeIterator> = LinkedHashMap()
    val reversedView: MutableCollection<TreeIterator> = animators.sequencedValues().reversed()
    val forceUpdateAnimation = AtomicBoolean()

    @Volatile
    var currentIterator: TreeIterator? = null

    @Volatile
    var beforeKeyframe: T? = null

    @Volatile
    var afterKeyframe: T? = null

    fun keyframeFinished(): Boolean {
        return delay <= 0
    }

    fun runningAnimation(): RunningAnimation? {
        val iterator = currentIterator
        return iterator?.animation
    }

    fun tick(ifEmpty: () -> Unit): Boolean {
        delay--
        if (animators.isEmpty()) {
            ifEmpty()
            return false
        }
        return shouldUpdateAnimation() && updateAnimation()
    }

    fun progress(): Float {
        val frame = frame()
        return if (frame == 0f) 0f else delay / frame
    }

    private fun shouldUpdateAnimation(): Boolean {
        return forceUpdateAnimation.compareAndSet(
            true,
            false
        ) || (afterKeyframe != null && keyframeFinished()) || delay % (50 / 10) == 0
    }

    private fun updateAnimation(): Boolean {
        synchronized(animators) {
            val iterator = reversedView.iterator()
            while(iterator.hasNext()) {
                val next = iterator.next()
                if (!next.asBoolean) continue
                if (currentIterator == null) {
                    if(updateKeyframe(iterator,next)) {
                        currentIterator = next
                        return true
                    }
                } else if (currentIterator != null) {
                    if (updateKeyframe(iterator,next)) {
                        currentIterator!!.clear()
                        currentIterator = next
                        return true
                    }
                } else if (keyframeFinished()) {
                    if (updateKeyframe(iterator,next)) {
                        return true
                    }
                } else return false
            }
        }
        return setAfterKeyframe(null)
    }

    private fun updateKeyframe(iterator: MutableIterator<TreeIterator>, next: TreeIterator): Boolean {
        if (!next.hasNext()) {
            // TODO: EVENT HANDLE
            iterator.remove()
            return false
        } else return setAfterKeyframe(next.next())
    }

    private fun setAfterKeyframe(next: T?): Boolean {
        if (afterKeyframe == next) return false
        beforeKeyframe = afterKeyframe
        afterKeyframe = next
        setConsumer(beforeKeyframe!!, afterKeyframe!!)
        delay = frame().roundToInt()
        return true
    }

    fun frame(): Float {
        return afterKeyframe?.let { 20 * 5 * (it.time + FRAME_EPSILON) } ?: 0f
    }

    inner class TreeIterator(
        val animation: RunningAnimation,
        val iterator: AnimationIterator<T>,
        val modifier: AnimationModifier,
    ) : BooleanSupplier {

        val previous: T = afterKeyframe ?: initialValue

        var ended = false
        var started = false

        override fun getAsBoolean(): Boolean {
            return modifier.predicateValue()
        }

        fun hasNext(): Boolean {
            return iterator.hasNext() || (modifier.end > 0 && !ended)
        }

        fun next(): T {
            if (!started) {
                started = true
                return mapper(iterator.next(), MappingState.START, modifier.start / 20f)
            }
            if (!iterator.hasNext()) {
                ended = true
                return mapper(previous, MappingState.END, modifier.end / 20f)
            }
            val next = iterator.next()
            return mapper(next, MappingState.PROGRESS, next.time / modifier.speedValue())
        }

        fun clear() {
            iterator.clear()
            val b = !iterator.hasNext()
            started = b
            ended = b
        }
    }

    enum class MappingState {
        START,
        PROGRESS,
        END,
    }

    data class RunningAnimation(val name: String, val type: AnimationIterator.Type)
}

typealias AnimationMapper<T> = (T, MappingState, Float) -> T
