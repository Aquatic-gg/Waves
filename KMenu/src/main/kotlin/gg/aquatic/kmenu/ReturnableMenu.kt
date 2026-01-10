package gg.aquatic.kmenu

import gg.aquatic.kmenu.coroutine.KMenuCtx
import gg.aquatic.kmenu.menu.Menu
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

interface ReturnableMenu {

    val returnTo: Menu

    suspend fun openPrevious(player: Player) = withContext(KMenuCtx) {
        returnTo.open(player)
    }
}