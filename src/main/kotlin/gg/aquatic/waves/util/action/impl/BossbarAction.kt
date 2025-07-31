package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.bossbar.AquaticBossBar
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.runLaterSync
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player

@RegisterAction("bossbar")
class BossbarAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val message = (args.string("message") { str -> textUpdater(binder, str)}!!).updatePAPIPlaceholders(binder)
        val progress = args.float("progress") { str -> textUpdater(binder, str)} ?: 0.0f
        val color = BossBar.Color.valueOf((args.string("color") { str -> textUpdater(binder, str)} ?: "BLUE").uppercase())
        val style = BossBar.Overlay.valueOf((args.string("style") { str -> textUpdater(binder, str)} ?: "SOLID").uppercase())

        val bossBar =
            AquaticBossBar(textUpdater(binder, message).toMMComponent(), color, style, mutableSetOf(), progress)
        val duration = args.int("duration") { str -> textUpdater(binder, str)} ?: 60

        bossBar.addViewer(binder)
        runLaterSync(duration.toLong()) {
            bossBar.removeViewer(binder)
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = arguments {
        primitive("message", "", true)
        primitive("progress", 0.0f)
        primitive("color", "BLUE")
        primitive("style", "SOLID")
        primitive("duration", 60, true)
    }
    /*
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("message", "", true),
        PrimitiveObjectArgument("progress", 0.0, false),
        PrimitiveObjectArgument("color", "BLUE", false),
        PrimitiveObjectArgument("style", "SOLID", false),
        PrimitiveObjectArgument("duration", 60, true)
    )

     */
}