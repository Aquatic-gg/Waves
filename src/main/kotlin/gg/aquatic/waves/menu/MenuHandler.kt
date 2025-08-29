package gg.aquatic.waves.menu

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.event.event
import gg.aquatic.waves.inventory.InventoryManager
import gg.aquatic.waves.inventory.PacketInventory
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.util.runAsyncTimer
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.player.PlayerDropItemEvent

object MenuHandler : WavesModule {
    override val type: WaveModules = WaveModules.MENUS

    override fun initialize(waves: Waves) {
        event<AsyncPacketInventoryInteractEvent> {
            val inv = it.inventory
            if (inv !is AquaticMenu) return@event
            inv.onInteract(it)
        }

        event<InventoryClickEvent> {
            val inv = InventoryManager.openedInventories[it.whoClicked] ?: return@event
            if (inv !is AquaticMenu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<InventoryInteractEvent> {
            val inv = InventoryManager.openedInventories[it.whoClicked] ?: return@event
            if (inv !is AquaticMenu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<InventoryDragEvent> {
            val inv = InventoryManager.openedInventories[it.whoClicked] ?: return@event
            if (inv !is AquaticMenu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }
        event<PlayerDropItemEvent> {
            val inv = InventoryManager.openedInventories[it.player] ?: return@event
            if (inv !is AquaticMenu) return@event
            if (inv.cancelBukkitInteractions) {
                it.isCancelled = true
            }
        }

        runAsyncTimer(1, 1) { tickInventories() }
    }


    private fun tickInventories() {
        val ticked = hashSetOf<PacketInventory>()
        for (openedInventory in InventoryManager.openedInventories.values) {
            if (ticked.contains(openedInventory)) continue
            ticked += openedInventory
            if (openedInventory is AquaticMenu) {
                openedInventory.tick()
            }
        }
    }

    override fun disable(waves: Waves) {

    }
}