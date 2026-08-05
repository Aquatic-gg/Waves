package gg.aquatic.common.audience

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Predicate

class FilterAudience(
    val filter: Predicate<Player>
): AquaticAudience {

    override fun canBeApplied(player: Player): Boolean {
        return filter.test(player)
    }

    override val uuids: Collection<UUID>
        get() = Bukkit.getOnlinePlayers().filter { filter.test(it) }.map { it.uniqueId }
}