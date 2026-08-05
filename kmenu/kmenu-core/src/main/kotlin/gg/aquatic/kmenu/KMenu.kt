package gg.aquatic.kmenu

import gg.aquatic.kmenu.KMenu.initialize
import gg.aquatic.kmenu.inventory.InventoryHandler
import gg.aquatic.kmenu.inventory.PacketInventory
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.replace.PlaceholderContext
import gg.aquatic.replace.Placeholders
import gg.aquatic.snapshotmap.SuspendingSnapshotMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.bukkit.entity.Player
import kotlin.coroutines.CoroutineContext

/**
 * Entry point for initializing and accessing KMenu state.
 *
 * Call [initialize] once during plugin startup.
 */
object KMenu {
    val packetInventories = SuspendingSnapshotMap<Player, PacketInventory>()

    lateinit var scope: CoroutineScope
        private set

    val context: CoroutineContext
        get() = scope.coroutineContext.minusKey(Job)

    /** Initialize with an existing [CoroutineScope]. */
    fun initialize(scope: CoroutineScope) {
        this.scope = scope
        InventoryHandler.initialize()
    }

    /** Initialize with a raw [CoroutineContext]. */
    fun initialize(context: CoroutineContext) {
        initialize(CoroutineScope(context + SupervisorJob()))
    }
}

/** Returns the currently open packet inventory for this player, if any. */
fun Player.packetInventory(): PacketInventory? {
    return KMenu.packetInventories[this]
}

/** Initialize KMenu using an existing [CoroutineScope]. */
fun initializeKMenu(scope: CoroutineScope) {
    initialize(scope)
}

/** Initialize KMenu using a raw [CoroutineContext]. */
fun initializeKMenu(context: CoroutineContext) {
    initialize(context)
}

/** Placeholder context scoped to a private menu's player. */
fun PlaceholderContext.Companion.privateMenu(): PlaceholderContext<Menu> {
    return Placeholders.resolverFor(
        Menu::class.java, 5, Placeholders.Transform { m ->
            (m as PrivateMenu).player
        })
}