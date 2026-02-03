package gg.aquatic.kmenu.menu.settings

import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.replace.PlaceholderContext
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/** Settings bundle for creating a [PrivateMenu]. */
class PrivateMenuSettings(
    val type: InventoryType,
    val title: Component,
    val components: HashMap<String, IButtonSettings>
) {

    suspend fun create(player: Player, updater: PlaceholderContext<Menu>, cancelBukkitInteractions: Boolean = true) = PrivateMenu(title, type, player, cancelBukkitInteractions).apply {
        for (value in this@PrivateMenuSettings.components.values) {
            val button = value.create(updater)
            addComponent(button)
        }
    }

}
