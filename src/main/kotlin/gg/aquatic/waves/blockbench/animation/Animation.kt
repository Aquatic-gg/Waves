package gg.aquatic.waves.blockbench.animation

import gg.aquatic.waves.blockbench.animation.impl.BBTimeline


open class Animation(
    val loopMode: LoopMode,
    val length: Double,
    var speed: Double,
    val timelines: Map<String, BBTimeline>,
    val animationHandler: AnimationHandler
) {

    var time = .0
        private set

    var phase = AnimationPhase.PLAYING

    fun update(): Boolean {
        if (phase === AnimationPhase.END) {
            return false
        }
        when (loopMode) {
            LoopMode.ONCE -> if (time < length) {
                var newTime = time + speed/20.0
                if (newTime > length) {
                    newTime = length
                }
                time = newTime
                return true
            }

            LoopMode.LOOP -> {
                // +0.05d to also be able to get the last frame of the animation (+ 1tick)
                time = (time + speed / 20.0) % (length - (0.05*speed))
                return true
            }

            LoopMode.HOLD -> {
                var newTime = time + speed/20.0
                if (newTime > length) {
                    newTime = length
                }
                time = newTime
                return true
            }
        }
        this.phase = AnimationPhase.END
        return false
    }

}