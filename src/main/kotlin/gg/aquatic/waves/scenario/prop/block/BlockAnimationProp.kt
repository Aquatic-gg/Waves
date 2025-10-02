package gg.aquatic.waves.scenario.prop.block

import gg.aquatic.waves.fake.block.FakeBlock
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.blockLocation
import org.bukkit.util.Vector

class BlockAnimationProp(
    override val scenario: Scenario,
    block: AquaticBlock,
    val offset: Vector
) : ScenarioProp {

    val packetBlock = FakeBlock(block, scenario.baseLocation.clone().add(offset).blockLocation(), 50, scenario.audience).apply {
        this.register()
    }

    override fun tick() {

    }

    override fun onEnd() {
        packetBlock.destroy()
    }
}