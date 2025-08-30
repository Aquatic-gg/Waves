package gg.aquatic.waves.scenario

import gg.aquatic.waves.util.audience.AquaticAudience
import net.kyori.adventure.key.Key
import org.bukkit.Location

abstract class Scenario {

    abstract val baseLocation: Location
    abstract val audience: AquaticAudience
    abstract val props: MutableMap<Key, ScenarioProp>

    var tick: Int = 0
    var isRunning: Boolean = true

    fun tick() {
        if (!isRunning) return
        onTick()
        tick++
    }

    open fun onTick() {

    }

    fun tickProps() {
        for (prop in props.values) {
            prop.tick()
        }
    }

    abstract val extraPlaceholders: MutableMap<Key, (String) -> String>
    abstract fun updatePlaceholders(original: String): String

    interface Phase {
        fun tick()
    }

    inline fun <reified T: Any> prop(id: Key): T? {
        return props[id] as? T
    }
    inline fun <reified T: Any> prop(id: String): T? {
        return prop(Key.key(id))
    }

    fun destroy() {
        isRunning = false
        for (prop in props.values) {
            prop.onEnd()
        }
        props.clear()
        onDestroy()
    }

    open fun onDestroy() {

    }
}