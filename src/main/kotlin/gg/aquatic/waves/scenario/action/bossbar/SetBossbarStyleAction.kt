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

@RegisterAction("set-bossbar-style")
class SetBossbarStyleAction : Action<Scenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "bossbar", true),
        PrimitiveObjectArgument("style", "solid", true),
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val style = args.string("style") { textUpdater(binder, it) } ?: return
        val prop = binder.prop<BossbarAnimationProp>(Key.key("bossbar:$id")) ?: return
        prop.bossBar.overlay = BossBar.Overlay.valueOf(style.uppercase())
    }
}