package gg.aquatic.waves.menu.settings

import gg.aquatic.waves.inventory.InventoryType
import gg.aquatic.waves.menu.AquaticMenu
import gg.aquatic.waves.menu.PrivateAquaticMenu
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PrivateMenuSettings(
    val type: InventoryType,
    val title: Component,
    val components: HashMap<String,IButtonSettings>
) {

    fun create(player: Player, updater: (String, AquaticMenu) -> String, cancelBukkitInteractions: Boolean = true) = PrivateAquaticMenu(title, type, player, cancelBukkitInteractions).apply {
        components += this@PrivateMenuSettings.components.mapValues { it.value.create(updater) }
    }

}