package gg.aquatic.kmenu

import gg.aquatic.kmenu.menu.Menu
import org.bukkit.entity.Player

/** Represents a menu that can return to another menu. */
interface ReturnableMenu {

    val returnTo: Menu

    suspend fun openPrevious(player: Player) {
        returnTo.open(player)
    }
}
