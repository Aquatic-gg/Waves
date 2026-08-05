package gg.aquatic.common.audience

import org.bukkit.entity.Player
import java.util.*

class WhitelistAudience(
    val whitelist: MutableList<UUID>
): MutableAquaticAudience() {
    override fun add(player: Player) {
        whitelist.add(player.uniqueId)
    }

    override fun remove(player: Player) {
        whitelist.remove(player.uniqueId)
    }

    override fun contains(player: Player): Boolean {
        return whitelist.contains(player.uniqueId)
    }

    override val uuids: Collection<UUID>
        get() {
            return whitelist
        }

    override fun canBeApplied(player: Player): Boolean {
        return whitelist.contains(player.uniqueId)
    }
}