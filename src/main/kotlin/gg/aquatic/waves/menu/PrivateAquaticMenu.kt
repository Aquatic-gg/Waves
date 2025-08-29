package gg.aquatic.waves.menu

import gg.aquatic.waves.inventory.InventoryManager
import gg.aquatic.waves.inventory.InventoryType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

open class PrivateAquaticMenu(
    title: Component, type: InventoryType, val player: Player,
    cancelBukkitInteractions: Boolean,
) : AquaticMenu(title, type, cancelBukkitInteractions) {

    fun open() {
        InventoryManager.openMenu(player, this)
    }
}