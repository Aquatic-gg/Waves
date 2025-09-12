package gg.aquatic.waves.scenario.action.bossbar

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.scenario.prop.BossbarAnimationProp
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.toMMComponent
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key

@RegisterAction("set-bossbar-data")
class SetBossbarDataAction: Action<PlayerScenario> {
    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String,
    ) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val color = args.string("color")
        val progress = args.float("progress")
        val style = args.string("style")
        val text = args.string("text")

        val prop = binder.prop<BossbarAnimationProp>(Key.key("bossbar:$id")) as? BossbarAnimationProp ?: return
        color?.let { prop.bossBar.color = BossBar.Color.valueOf(it.uppercase()) }
        progress?.let { prop.bossBar.progress = it }
        style?.let { prop.bossBar.overlay = BossBar.Overlay.valueOf(it.uppercase()) }
        text?.let { prop.bossBar.message = binder.updatePlaceholders(it).toMMComponent() }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "bossbar", true),
        PrimitiveObjectArgument("color", null, false),
        PrimitiveObjectArgument("progress", null, false),
        PrimitiveObjectArgument("style", null, false),
        PrimitiveObjectArgument("text", null, false),
    )
}