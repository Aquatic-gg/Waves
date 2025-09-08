package gg.aquatic.waves.scenario.prop

import gg.aquatic.waves.hologram.AquaticHologram
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp

class HologramScenarioProp(
    override val scenario: Scenario,
    val hologram: AquaticHologram
) : ScenarioProp {

    override fun tick() {

    }

    override fun onEnd() {
        hologram.destroy()
    }
}