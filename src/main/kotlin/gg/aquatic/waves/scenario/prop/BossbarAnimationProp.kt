package gg.aquatic.waves.scenario.prop

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.scenario.PlayerScenarioProp
import gg.aquatic.waves.util.bossbar.AquaticBossBar
import gg.aquatic.waves.util.toMMComponent
import net.kyori.adventure.bossbar.BossBar

class BossbarAnimationProp(
    override val scenario: PlayerScenario,
    @Volatile var text: String,
    color: BossBar.Color,
    style: BossBar.Overlay,
    progress: Float,
    //var textUpdater: (Player, String) -> String = { _, str -> str }
) : PlayerScenarioProp {

    val bossBar = AquaticBossBar(
        scenario.updatePlaceholders(text).toMMComponent(), color, style, mutableSetOf(), progress)

    init {
        bossBar.addViewer(scenario.player)
    }

    override fun tick() {
        val newMsg = scenario.updatePlaceholders(text).toMMComponent()
        bossBar.message = newMsg
    }

    override fun onEnd() {
        bossBar.removeViewer(scenario.player)
    }
}