package gg.aquatic.waves.menu.settings

import gg.aquatic.waves.inventory.ButtonType
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectsWithConditions
import org.bukkit.entity.Player

class ClickSettings(
    val clicks: HashMap<ButtonType,MutableList<ConfiguredExecutableObjectsWithConditions<Player,Unit>>>,
) {

    fun handleClick(event: AsyncPacketInventoryInteractEvent, updater: (Player, String) -> String) {
        val type = event.buttonType
        val actions = clicks[type] ?: return
        for (action in actions) {
            action.tryExecute(event.viewer.player, updater)
        }
    }
}