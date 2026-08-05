package gg.aquatic.common.audience

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

interface AquaticAudience {

    val uuids: Collection<UUID>
        get() = asOnlinePlayers().map { it.uniqueId }

    fun canBeApplied(player: Player): Boolean

    fun asOnlinePlayers(): List<Player> {
        return Bukkit.getOnlinePlayers().filter { uuids.contains(it.uniqueId) }
    }
}