package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.DayOfWeek
import java.time.LocalDate

object DayOfWeekCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val allowed = args.stringOrCollection("days")
            ?.mapNotNull { raw -> runCatching { DayOfWeek.valueOf(raw.trim().uppercase()) }.getOrNull() }
            ?.toSet()
            ?: return false
        return LocalDate.now().dayOfWeek in allowed
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("days", "MONDAY", true),
    )
}
