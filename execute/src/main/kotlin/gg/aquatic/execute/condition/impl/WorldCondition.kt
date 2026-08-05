package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.condition.type.PlayerCondition
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

object WorldCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean =
        withContext(BukkitCtx.ofEntity(binder)) {
            val allowedWorlds = args.stringOrCollection("worlds")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                ?: return@withContext false
            return@withContext binder.world.name in allowedWorlds
        }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("worlds", listOf("world"), true),
    )
}
