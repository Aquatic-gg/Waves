package gg.aquatic.treepapi

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class PlaceholderArgumentContext(
    val binder: OfflinePlayer,
    val arguments: Map<String, Any?>
) {
    inline fun <reified T> get(id: String): T? = arguments[id] as? T
    fun string(id: String): String? = get<String>(id)
    fun player(id: String): Player? = get<Player>(id)
}