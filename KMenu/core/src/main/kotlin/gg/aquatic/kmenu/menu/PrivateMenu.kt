package gg.aquatic.kmenu.menu

import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.menu.settings.PrivateMenuSettings
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/** Menu instance bound to a single player. */
open class PrivateMenu(
    title: Component, type: InventoryType, val player: Player,
    cancelBukkitInteractions: Boolean,
) : Menu(title, type, cancelBukkitInteractions) {

    constructor(player: Player, settings: PrivateMenuSettings, cancelBukkitInteractions: Boolean) : this(
        settings.title,
        settings.type,
        player,
        cancelBukkitInteractions
    )

    suspend fun open() = open(player)

    fun close() {
        close(player)
    }
}
