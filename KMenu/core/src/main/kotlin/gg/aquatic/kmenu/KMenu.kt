package gg.aquatic.kmenu

import gg.aquatic.kmenu.inventory.InventoryHandler
import gg.aquatic.kmenu.inventory.PacketInventory
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.replace.placeholder.PlaceholderContext
import gg.aquatic.replace.placeholder.Placeholders
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Entry point for initializing and accessing KMenu state.
 *
 * Call [initialize] once during plugin startup.
 */
object KMenu {
    val packetInventories = ConcurrentHashMap<Player, PacketInventory>()
    lateinit var plugin: Plugin
        private set

    lateinit var scope: CoroutineScope
        private set

    val context: CoroutineContext
        get() = scope.coroutineContext.minusKey(Job)

    /** Initialize with an existing [CoroutineScope]. */
    fun initialize(plugin: Plugin, scope: CoroutineScope) {
        this.plugin = plugin
        this.scope = scope
        InventoryHandler.initialize(plugin)
    }

    /** Initialize with a raw [CoroutineContext]. */
    fun initialize(plugin: Plugin, context: CoroutineContext) {
        initialize(plugin, CoroutineScope(context + SupervisorJob()))
    }
}

/** Returns the currently open packet inventory for this player, if any. */
fun Player.packetInventory(): PacketInventory? {
    return KMenu.packetInventories[this]
}

/** Initialize KMenu using an existing [CoroutineScope]. */
fun initializeKMenu(plugin: Plugin, scope: CoroutineScope) {
    KMenu.initialize(plugin, scope)
}

/** Initialize KMenu using a raw [CoroutineContext]. */
fun initializeKMenu(plugin: Plugin, context: CoroutineContext) {
    KMenu.initialize(plugin, context)
}

/** Placeholder context scoped to a private menu's player. */
fun PlaceholderContext.Companion.privateMenu(): PlaceholderContext<Menu> {
    return Placeholders.resolverFor(
        Menu::class.java, 5, Placeholders.Transform { m ->
            (m as PrivateMenu).player
        })
}
