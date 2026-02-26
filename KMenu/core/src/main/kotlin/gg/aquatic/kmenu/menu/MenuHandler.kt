package gg.aquatic.kmenu.menu

import gg.aquatic.common.event
import gg.aquatic.common.ticker.GlobalTicker
import gg.aquatic.kevent.subscribe
import gg.aquatic.kmenu.KMenu
import gg.aquatic.kmenu.inventory.InventoryHandler
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.packetInventory
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerDropItemEvent

internal object MenuHandler {
    fun initialize() {
        GlobalTicker.runRepeatFixedDelay(20L) {
            tickInventories()
        }

        InventoryHandler.eventBus.subscribe<AsyncPacketInventoryInteractEvent> {
            val inv = it.inventory
            if (inv !is Menu) return@subscribe
            inv.onInteract(it)
        }
        initializeBukkitEvents()
    }

    private fun initializeBukkitEvents() {
        event<InventoryClickEvent> { handleBukkitInteraction(it) }
        event<InventoryInteractEvent> { handleBukkitInteraction(it) }
        event<InventoryDragEvent> { handleBukkitInteraction(it) }
        event<PlayerDropItemEvent> {
            val inv = it.player.packetInventory() as? Menu ?: return@event
            if (inv.cancelBukkitInteractions) it.isCancelled = true
        }
    }

    private fun handleBukkitInteraction(event: InventoryInteractEvent) {
        val inv = (event.whoClicked as? Player)?.packetInventory() as? Menu ?: return
        if (inv.cancelBukkitInteractions) {
            event.isCancelled = true
        }
    }

    private suspend fun tickInventories() = withContext(KMenu.context) {
        val ticked = hashSetOf<Menu>()

        for (player in Bukkit.getOnlinePlayers()) {
            val openedInventory = player.packetInventory() as? Menu ?: continue
            if (ticked.add(openedInventory)) {
                openedInventory.tick()
            }
        }
    }
}
