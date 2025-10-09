package gg.aquatic.execute.requirement.impl

import gg.aquatic.execute.Condition
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import org.bukkit.entity.Player

object PermissionCondition: Condition<Player> {
    override fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val permission = args.string("permission") ?: return false
        return binder.hasPermission(permission)
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("permission", "example", true),
    )
}