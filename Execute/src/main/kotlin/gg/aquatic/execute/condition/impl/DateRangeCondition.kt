package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalDate

object DateRangeCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val start = args.string("start")?.let(::parseDate) ?: return false
        val end = args.string("end")?.let(::parseDate) ?: return false
        val now = LocalDate.now()
        return !now.isBefore(start) && !now.isAfter(end)
    }

    private fun parseDate(raw: String): LocalDate? {
        return runCatching { LocalDate.parse(raw.trim()) }.getOrNull()
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("start", "2026-01-01", true),
        PrimitiveObjectArgument("end", "2026-12-31", true),
    )
}
