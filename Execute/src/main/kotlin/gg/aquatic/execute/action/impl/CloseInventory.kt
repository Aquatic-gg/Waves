package gg.aquatic.execute.action.impl

import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.coroutine.BukkitCtx
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

object CloseInventory: Action<Player> {
    override suspend fun execute(
        binder: Player,
        args: ArgumentContext<Player>
    ) = withContext(BukkitCtx) {
        binder.closeInventory()
    }

    override val arguments: List<ObjectArgument<*>> = listOf()
}