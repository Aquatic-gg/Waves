package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.temporal.WeekFields

object WeekParityCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val parity = args.string("parity")?.trim()?.lowercase() ?: return false
        val weekOfYear = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
        return when (parity) {
            "odd" -> weekOfYear % 2 == 1
            "even" -> weekOfYear % 2 == 0
            else -> false
        }
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("parity", "odd", true),
    )
}
