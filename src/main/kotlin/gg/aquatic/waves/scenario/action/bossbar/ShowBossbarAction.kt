package gg.aquatic.waves.scenario.action.bossbar

import gg.aquatic.waves.scenario.prop.BossbarAnimationProp
import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key

@RegisterAction("show-bossbar")
class ShowBossbarAction : Action<PlayerScenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "bossbar", true),
        PrimitiveObjectArgument("message", "", true),
        PrimitiveObjectArgument("color", "white", false),
        PrimitiveObjectArgument("style", "solid", false),
        PrimitiveObjectArgument("progress", 1.0, false),
    )

    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String
    ) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val message = args.string("message") ?: return
        val color = args.string("color") { textUpdater(binder, it) } ?: return
        val style = args.string("style") { textUpdater(binder, it) } ?: return
        val progress = args.float("progress") { textUpdater(binder, it) } ?: 1.0f

        val prop = BossbarAnimationProp(
            binder,
            message,
            BossBar.Color.valueOf(color.uppercase()),
            BossBar.Overlay.valueOf(style.uppercase()),
            progress
        )
        val previous = binder.props.put(Key.key("bossbar:$id"), prop)
        previous?.onEnd()
    }
}