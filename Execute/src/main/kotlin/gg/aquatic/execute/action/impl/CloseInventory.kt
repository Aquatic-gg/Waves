package gg.aquatic.execute.action.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.Action
import gg.aquatic.execute.action.type.PlayerAction
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

object CloseInventory: PlayerAction() {
    override suspend fun execute(
        binder: Player,
        args: ArgumentContext<Player>
    ) = withContext(BukkitCtx.ofEntity(binder)) {
        binder.closeInventory()
    }

    override val arguments: List<ObjectArgument<*>> = listOf()
}