package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.condition.type.PlayerCondition
import kotlinx.coroutines.withContext
import org.bukkit.Material
import org.bukkit.entity.Player

object HasEmptyInventorySlotCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean =
        withContext(BukkitCtx.ofEntity(binder)) {
            val required = (args.int("amount") ?: 1).coerceAtLeast(1)
            val emptySlots = binder.inventory.storageContents.count { it == null || it.type == Material.AIR }
            return@withContext emptySlots >= required
        }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("amount", 1, required = false),
    )
}
