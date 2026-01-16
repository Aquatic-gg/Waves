package gg.aquatic.kmenu

import gg.aquatic.kmenu.inventory.InventoryModule
import gg.aquatic.kmenu.inventory.PacketInventory
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.replace.PlaceholderContext
import gg.aquatic.replace.Placeholders
import io.github.charlietap.cachemap.cacheMapOf
import org.bukkit.entity.Player

object KMenu {
    val packetInventories = cacheMapOf<Player, PacketInventory>()

    fun initialize() {
        InventoryModule.onLoad()
    }
}

fun Player.packetInventory(): PacketInventory? {
    return KMenu.packetInventories[this]
}

fun PlaceholderContext.Companion.privateMenu(): PlaceholderContext<Menu> {
    return Placeholders.resolverFor(
        Menu::class.java, 5, Placeholders.Transform { m ->
            (m as PrivateMenu).player
        })
}