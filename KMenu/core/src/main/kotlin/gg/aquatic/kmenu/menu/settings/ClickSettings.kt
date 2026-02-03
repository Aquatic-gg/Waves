package gg.aquatic.kmenu.menu.settings

import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import org.bukkit.entity.Player

typealias ClickAction = suspend (Player, (Player, String) -> String) -> Unit

/** Per-button click handlers keyed by click type. */
class ClickSettings(
    val clicks: HashMap<ButtonType, MutableList<ClickAction>>,
) {

    suspend fun handleClick(event: AsyncPacketInventoryInteractEvent, updater: (Player, String) -> String) {
        val type = event.buttonType
        val actions = clicks[type] ?: return

        for (action in actions) {
            action(event.viewer.player, updater)
        }
    }
}
