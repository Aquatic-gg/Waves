package gg.aquatic.waves.blockbench.template

import gg.aquatic.waves.blockbench.animation.impl.BBTimeline
import gg.aquatic.waves.blockbench.animation.LoopMode
import org.bukkit.util.Vector

class BBAnimationTemplate(
    val name: String,
    val length: Double,
    val loopMode: LoopMode,
    val timelines: Map<String,BBTimeline>,
    val scripts: Map<Int,ParticleScript>
) {

    fun getRotation(bone: String, time: Double): Vector {
        return timelines[bone]?.getRotationFrame(time) ?: Vector()
    }

    fun getPosition(bone: String, time: Double): Vector {
        return timelines[bone]?.getPositionFrame(time) ?: Vector()
    }

    class ParticleScript(
        val script: String
    )
}