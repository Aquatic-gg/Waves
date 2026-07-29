package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object OnlinePlayerCountCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val min = (args.int("min") ?: 0).coerceAtLeast(0)
        val max = args.int("max")
        val online = Bukkit.getOnlinePlayers().size

        if (online < min) {
            return false
        }

        return max == null || online <= max
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("min", 0, required = false),
        PrimitiveObjectArgument("max", null, required = false),
    )
}
