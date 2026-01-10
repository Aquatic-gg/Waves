package gg.aquatic.kmenu.menu.settings

import gg.aquatic.execute.ConditionalActionsHandle
import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import org.bukkit.entity.Player

class ClickSettings(
    val clicks: HashMap<ButtonType, MutableList<ConditionalActionsHandle<Player>>>,
) {

    suspend fun handleClick(event: AsyncPacketInventoryInteractEvent, updater: (Player, String) -> String) {
        val type = event.buttonType
        val actions = clicks[type] ?: return

        for (action in actions) {
            action.tryExecute(event.viewer.player, updater)
        }
    }
}