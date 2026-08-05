package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalDate

object DayOfMonthCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val allowed = args.arguments.intOrCollection("days")
            ?.filter { it in 1..31 }
            ?.toSet()
            ?: return false
        return LocalDate.now().dayOfMonth in allowed
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("days", 1, true),
    )
}
