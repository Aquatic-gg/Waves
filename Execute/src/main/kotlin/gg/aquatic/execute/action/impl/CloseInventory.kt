package gg.aquatic.execute.action.impl

import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.Action
import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

object CloseInventory: Action<Player> {
    override suspend fun execute(
        binder: Player,
        args: ArgumentContext<Player>
    ) = withContext(BukkitCtx.ofEntity(binder)) {
        binder.closeInventory()
    }

    override val arguments: List<ObjectArgument<*>> = listOf()
}