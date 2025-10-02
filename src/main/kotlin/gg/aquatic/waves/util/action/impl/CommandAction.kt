package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.task.BukkitScope
import gg.aquatic.waves.util.updatePAPIPlaceholders
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@RegisterAction("command")
class CommandAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val commands = args.stringOrCollection("command") ?: return
        val executor = if (args.boolean("player-executor") == true) binder else Bukkit.getConsoleSender()

        val run = {
            for (cmd in commands) {
                val command = textUpdater(binder, cmd.updatePAPIPlaceholders(binder))
                if (command.isEmpty() || command.isBlank()) continue
                Bukkit.dispatchCommand(
                    executor,
                    command
                )
            }
        }

        if (Bukkit.getServer().isPrimaryThread) {
            run()
        } else {
            BukkitScope.launch {
                for (cmd in commands) {
                    val command = textUpdater(binder, cmd.updatePAPIPlaceholders(binder))
                    if (command.isEmpty() || command.isBlank()) continue
                    Bukkit.dispatchCommand(
                        executor,
                        command
                    )
                }
            }
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("command", "", true),
        PrimitiveObjectArgument("player-executor", false, required = false)
    )
}