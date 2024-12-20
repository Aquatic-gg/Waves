package gg.aquatic.waves.util.inventory

import gg.aquatic.aquaticseries.lib.action.ConfiguredAction
import gg.aquatic.aquaticseries.lib.adapt.AquaticString
import gg.aquatic.aquaticseries.lib.betterinventory2.AquaticInventory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import java.util.function.BiFunction

class InventorySettings(
    val title: AquaticString,
    val size: Int,
    val inventoryType: InventoryType?,
    val buttons: List<ButtonSettings>,
    val onOpen: List<ConfiguredAction<Player>>,
    val onClose: List<ConfiguredAction<Player>>
) {
    fun create(id: String, textUpdater: BiFunction<Player, String, String>): AquaticInventory {
        val createdButtons = buttons.map { it.create(textUpdater) {} }
        val inv = AquaticInventory(
            title,
            size,
            inventoryType,
            { player, event ->
                for (configuredAction in onOpen) {
                    configuredAction.run(player, textUpdater)
                }
            },
            { player, event ->
                for (configuredAction in onClose) {
                    configuredAction.run(player, textUpdater)
                }
            },
            { player, event ->

            }
        )
        for (createdButton in createdButtons) {
            inv.addComponent(
                createdButton
            )
        }
        inv.id = id
        return inv
    }

}