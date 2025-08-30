package gg.aquatic.waves.scenario.prop.timer

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject

class LaterActionsAnimationProp<T: Scenario>(
    override val scenario: T, val actions: Collection<ConfiguredExecutableObject<T, Unit>>, val runAfter: Int
) : ScenarioProp {

    var tick = 0
        private set

    private var finished = false

    override fun tick() {
        if (finished) return
        if (tick >= runAfter) {
            for (action in actions) {
                action.execute(scenario) { a, str -> a.updatePlaceholders(str) }
            }
        }
    }

    override fun onEnd() {

    }
}