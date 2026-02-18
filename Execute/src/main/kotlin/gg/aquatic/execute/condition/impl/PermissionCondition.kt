package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.Condition
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player

object PermissionCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val permission = args.string("permission") ?: return false
        return binder.hasPermission(permission)
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("permission", "example", true),
    )
}