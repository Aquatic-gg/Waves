package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.condition.type.PlayerCondition
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.Month

object MonthCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean {
        val allowed = args.stringOrCollection("months")
            ?.mapNotNull { raw -> runCatching { Month.valueOf(raw.trim().uppercase()) }.getOrNull() }
            ?.toSet()
            ?: return false
        return LocalDate.now().month in allowed
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("months", "JANUARY", true),
    )
}
