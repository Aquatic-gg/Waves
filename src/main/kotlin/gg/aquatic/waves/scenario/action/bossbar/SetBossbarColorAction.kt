package gg.aquatic.waves.scenario.action.bossbar

import gg.aquatic.waves.scenario.prop.BossbarAnimationProp
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key

@RegisterAction("set-bossbar-color")
class SetBossbarColorAction : Action<Scenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "bossbar", true),
        PrimitiveObjectArgument("color", "blue", true),
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val color = args.string("color") { textUpdater(binder, it) } ?: return
        val key = Key.key("bossbar:$id")
        val prop = binder.prop<BossbarAnimationProp>(key) ?: return
        prop.bossBar.color = BossBar.Color.valueOf(color.uppercase())
    }
}