package gg.aquatic.waves.scenario.prop.timer

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.util.collection.executeActions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject

class TimedActionsAnimationProp<T: Scenario>(
    override val scenario: T,
    val actions: HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>
) :
    ScenarioProp {

    var tick = 0
        private set

    override fun tick() {
        actions[tick]?.forEach { it.execute(scenario) { a, str -> a.updatePlaceholders(str) } }
        tick++
    }

    override fun onEnd() {
    }
}