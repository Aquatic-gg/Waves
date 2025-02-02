package gg.aquatic.waves.util.action.impl

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.bossbar.AquaticBossBar
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.runLaterSync
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.entity.Player
import java.util.*

class BossbarAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val message = (args.string("message") { str -> textUpdater(binder, str)}!!).updatePAPIPlaceholders(binder)
        val progress = args.float("progress") { str -> textUpdater(binder, str)} ?: 0.0f
        val color = BossBar.Color.valueOf((args.string("color") { str -> textUpdater(binder, str)} ?: "BLUE").uppercase())
        val style = BossBar.Overlay.valueOf((args.string("style") { str -> textUpdater(binder, str)} ?: "SOLID").uppercase())

        val bossBar =
            AquaticBossBar(textUpdater(binder, message).toMMComponent(), color, style, mutableSetOf(), progress)
        val duration = args.int("duration") { str -> textUpdater(binder, str)} ?: 60

        WrapperPlayServerBossBar(UUID.randomUUID(), WrapperPlayServerBossBar.Action.ADD).color

        bossBar.addViewer(binder)
        runLaterSync(duration.toLong()) {
            bossBar.removeViewer(binder)
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("message", "", true),
        PrimitiveObjectArgument("progress", 0.0, false),
        PrimitiveObjectArgument("color", "BLUE", false),
        PrimitiveObjectArgument("style", "SOLID", false),
        PrimitiveObjectArgument("duration", 60, true)
    )
}