package gg.aquatic.common.audience

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class BlacklistAudience(
    val blacklist: MutableList<UUID>
): MutableAquaticAudience() {
    override fun add(player: Player) {
        blacklist.add(player.uniqueId)
    }

    override fun remove(player: Player) {
        blacklist.remove(player.uniqueId)
    }

    override fun contains(player: Player): Boolean {
        return blacklist.contains(player.uniqueId)
    }

    override val uuids: Collection<UUID>
        get() {
            return Bukkit.getOnlinePlayers().map { it.uniqueId }.filter { !blacklist.contains(it) }
        }

    override fun canBeApplied(player: Player): Boolean {
        return !blacklist.contains(player.uniqueId)
    }

}