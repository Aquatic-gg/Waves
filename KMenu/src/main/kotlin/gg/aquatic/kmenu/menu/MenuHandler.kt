package gg.aquatic.kmenu.menu

import gg.aquatic.kevent.subscribe
import gg.aquatic.kmenu.inventory.InventoryModule
import gg.aquatic.kmenu.inventory.PacketInventory
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.packetInventory
import gg.aquatic.stacked.event
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerDropItemEvent

object MenuHandler {
    fun initialize() {

        InventoryModule.eventBus.subscribe<AsyncPacketInventoryInteractEvent> {
            val inv = it.inventory
            if (inv !is Menu) return@subscribe
            inv.onInteract(it)
        }

        event<InventoryClickEvent> {
            val inv = (it.whoClicked as? Player)?.packetInventory() ?: return@event
            if (inv !is Menu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<InventoryInteractEvent> {
            val inv = (it.whoClicked as? Player)?.packetInventory() ?: return@event
            if (inv !is Menu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<InventoryDragEvent> {
            val inv = (it.whoClicked as? Player)?.packetInventory() ?: return@event
            if (inv !is Menu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<PlayerDropItemEvent> {
            val inv = it.player.packetInventory() ?: return@event
            if (inv !is Menu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
    }

    suspend fun tick() {
        tickInventories()
    }


    private suspend fun tickInventories() {
        val ticked = hashSetOf<Menu>()

        for (player in Bukkit.getOnlinePlayers()) {
            val openedInventory = player.packetInventory() as? Menu ?: continue
            if (ticked.add(openedInventory)) {
                openedInventory.tick()
            }
        }
    }
}