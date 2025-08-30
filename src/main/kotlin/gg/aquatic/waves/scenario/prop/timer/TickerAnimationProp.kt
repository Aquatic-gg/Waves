package gg.aquatic.waves.scenario.prop.timer

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.util.collection.executeActions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import net.kyori.adventure.key.Key

class TickerAnimationProp<T: Scenario>(
    override val scenario: T,
    val id: String,
    val tickEvery: Int,
    val actions: Collection<ConfiguredExecutableObject<T, Unit>>,
    val repeatLimit: Int) : ScenarioProp {

    var tick = 0
    var actualTick = 0

    init {
        scenario.extraPlaceholders += Key.key("tick:$id") to { str ->
            str.replace("%tick:$id%", actualTick.toString())
        }
    }

    override fun tick() {
        if (repeatLimit > 0 && actualTick >= repeatLimit) {
            return
        }
        tick++
        if (tick >= tickEvery) {
            tick = 0
            actualTick++
            for (action in actions) {
                action.execute(scenario) { a, str -> a.updatePlaceholders(str) }
            }
        }
    }

    override fun onEnd() {

    }
}