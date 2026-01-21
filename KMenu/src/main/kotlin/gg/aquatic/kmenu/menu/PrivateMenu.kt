package gg.aquatic.kmenu.menu

import gg.aquatic.kmenu.coroutine.KMenuCtx
import gg.aquatic.kmenu.inventory.InventoryHandler
import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.menu.settings.PrivateMenuSettings
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

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

    open suspend fun open() = withContext(KMenuCtx) {
        InventoryHandler.openMenu(player, this@PrivateMenu)
    }

    fun close() {
        close(player)
    }
}