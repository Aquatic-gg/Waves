package gg.aquatic.waves.scenario.prop.bbanim

import gg.aquatic.waves.blockbench.BlockBenchAnimationHandler
import gg.aquatic.waves.blockbench.handle.BBTemplateHandle
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.util.audience.AquaticAudience
import org.bukkit.Location

class BBAnimProp(
    override val scenario: Scenario,
    val templateHandle: BBTemplateHandle

) : ScenarioProp {

    override fun tick() {

    }

    override fun onEnd() {
        BlockBenchAnimationHandler.spawned -= templateHandle
    }

    class BonePart(val bone: String, val location: Location, val audience: AquaticAudience)
}