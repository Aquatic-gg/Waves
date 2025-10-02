package gg.aquatic.waves.blockbench.animation

import gg.aquatic.waves.blockbench.handle.BBTemplateHandle
import org.bukkit.util.Vector

class AnimationHandler(
    val BBTemplateHandle: BBTemplateHandle
) {

    var runningAnimation: Animation? = null

    fun update() {
        val a = runningAnimation ?: return
        if (!a.update()) {
            runningAnimation = null
        }
    }

    fun playAnimation(name: String, speed: Double){
        val animation = BBTemplateHandle.template.animations[name] ?: return
        runningAnimation = Animation(animation.loopMode, animation.length, speed, animation.timelines, this)
    }

    fun getPosition(name: String): Vector {
        val a = runningAnimation ?: return Vector()
        return a.timelines[name]?.getPositionFrame(a.time) ?: Vector()
    }


    fun getRotation(name: String): Vector {
        val a = runningAnimation ?: return Vector()

        val tl = a.timelines[name] ?: return Vector()
        return tl.getRotationFrame(a.time)
    }

    fun getScale(name: String): Vector {
        val a = runningAnimation ?: return Vector(1.0, 1.0, 1.0)

        val tl = a.timelines[name] ?: return Vector(1.0, 1.0, 1.0)
        return tl.getScaleFrame(a.time)
    }

    fun stop() {
        runningAnimation = null
    }

}