package gg.aquatic.waves.scenario.action.bossbar

import gg.aquatic.waves.scenario.prop.BossbarAnimationProp
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key

@RegisterAction("set-bossbar-progress")
class SetBossbarProgressAction  : Action<Scenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "bossbar", true),
        PrimitiveObjectArgument("progress", 0.0, true),
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val progress = args.float("progress") { textUpdater(binder, it) } ?: return
        val key = Key.key("bossbar:$id")
        val prop = binder.prop<BossbarAnimationProp>(key) ?: return
        prop.bossBar.progress = progress
    }
}