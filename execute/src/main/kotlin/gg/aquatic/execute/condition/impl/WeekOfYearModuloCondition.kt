package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.temporal.WeekFields

object WeekOfYearModuloCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val modulo = (args.int("modulo") ?: return false).coerceAtLeast(1)
        val expected = (args.int("equals") ?: return false).coerceIn(0, modulo - 1)
        val weekOfYear = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
        return weekOfYear % modulo == expected
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("modulo", 2, true),
        PrimitiveObjectArgument("equals", 0, true),
    )
}
