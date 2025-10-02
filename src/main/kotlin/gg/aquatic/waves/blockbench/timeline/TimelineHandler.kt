package gg.aquatic.waves.blockbench.timeline

import java.util.function.Consumer


class TimelineHandler {
    private val timelines: MutableMap<Class<out Keyframe>, Timeline<out Keyframe>> = HashMap()

    fun <T : Keyframe> addTimeline(clazz: Class<T>, timeline: Timeline<T>) {
        timelines[clazz] = timeline
    }

    fun <T : Keyframe> getTimeline(clazz: Class<T>): Timeline<T> {
        return timelines[clazz] as Timeline<T>
    }

    fun run(time: Double) {
        timelines.values.forEach(Consumer { tl: Timeline<out Keyframe> ->
            tl.run(
                time
            )
        })
    }
}