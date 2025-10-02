package gg.aquatic.waves.blockbench.timeline

import java.util.TreeMap

open class Timeline<T: Keyframe>(
    val timeline: TreeMap<Double,T> = TreeMap()
) {
    constructor() : this(TreeMap())

    fun addFrame(time: Double, frame: T) {
        timeline[time] = frame
    }

    fun run(time: Double) {
        val frame = timeline[time] ?: return
        frame.run()
    }

}