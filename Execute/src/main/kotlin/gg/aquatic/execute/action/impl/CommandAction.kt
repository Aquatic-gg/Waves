package gg.aquatic.execute.action.impl

import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.Action
import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object CommandAction : Action<Player> {

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) =
        withContext(BukkitCtx.ofEntity(binder)) {
            val commands = args.stringOrCollection("command") ?: return@withContext
            val executor = if (args.boolean("player-executor") == true) binder else Bukkit.getConsoleSender()

            for (cmd in commands) {
                if (cmd.isEmpty() || cmd.isBlank()) continue
                Bukkit.dispatchCommand(
                    executor,
                    cmd
                )
            }
        }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("command", "", true),
        PrimitiveObjectArgument("player-executor", false, required = false)
    )
}