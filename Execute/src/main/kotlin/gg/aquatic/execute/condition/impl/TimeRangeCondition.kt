package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalTime

object TimeRangeCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val start = args.string("start")?.let(::parseTime) ?: return false
        val end = args.string("end")?.let(::parseTime) ?: return false
        val now = LocalTime.now()

        return if (start <= end) {
            now >= start && now <= end
        } else {
            now >= start || now <= end
        }
    }

    private fun parseTime(raw: String): LocalTime? {
        return runCatching { LocalTime.parse(raw.trim()) }.getOrNull()
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("start", "09:00", true),
        PrimitiveObjectArgument("end", "18:00", true),
    )
}
