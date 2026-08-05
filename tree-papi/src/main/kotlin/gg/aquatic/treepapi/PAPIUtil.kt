package gg.aquatic.treepapi

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PAPIUtil {
    fun registerExtension(
        author: String,
        identifier: String,
        onRequest: (player: org.bukkit.OfflinePlayer, params: String) -> String
    ): PlaceholderExpansion? {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return null
        }
        val extension = object : PlaceholderExpansion() {
            override fun getIdentifier(): String = identifier
            override fun getAuthor(): String = author
            override fun getVersion(): String = "1.0.0"
            override fun persist(): Boolean = false

            override fun onRequest(player: org.bukkit.OfflinePlayer?, params: String): String? {
                return onRequest(player ?: return null, params)
            }
        }
        extension.register()
        return extension
    }
}

fun String.updatePAPIPlaceholders(player: Player): String {
    Bukkit.getPluginManager().getPlugin("PlaceholderAPI") ?: return this
    return PlaceholderAPI.setPlaceholders(player, this)
}