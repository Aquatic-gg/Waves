package gg.aquatic.kmenu.inventory

import gg.aquatic.kmenu.inventory.InventoryModule.playerSlotFromMenuSlot
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.sendPacket
import io.github.charlietap.cachemap.cacheMapOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

open class PacketInventory(
    title: Component,
    val type: InventoryType
) : Cloneable {

    val viewers = cacheMapOf<UUID, InventoryViewer>()
    val content = cacheMapOf<Int, ItemStack>()

    private val logicMutex = Mutex()

    val viewerPlayers: Array<Player>
        get() = viewers.values.map { it.player }.toTypedArray()

    var title: Component = title
        private set

    suspend fun updateTitle(newTitle: Component) = logicMutex.withLock {
        this.title = newTitle
        this.inventoryOpenPacket = createOpenPacket()

        for (player in viewerPlayers) {
            player.sendPacket(inventoryOpenPacket)
            val viewer = viewers[player.uniqueId] ?: continue
            InventoryModule.updateInventoryContent(this, viewer)
        }
    }

    var inventoryOpenPacket: Any = createOpenPacket()
        private set

    private fun createOpenPacket(): Any {
        return Pakket.handler.openWindowPacket(126, type.menuType, title)
    }

    fun sendInventoryOpenPacket(player: Player) {
        player.sendPacket(inventoryOpenPacket)
    }

    private fun addItem(slot: Int, item: ItemStack) {
        val previous = content[slot]
        if (previous != null && previous.isSimilar(item) && previous.amount == item.amount) {
            return
        }
        content[slot] = item
    }

    suspend fun setItem(slot: Int, item: ItemStack?, update: Boolean = true) {
        logicMutex.withLock {
            if (item == null) content.remove(slot) else content[slot] = item

            if (update) {
                if (slot > type.size + 36) return
                if (item == null) {
                    content.remove(slot)
                } else {
                    addItem(slot, item)
                }
                val packet = Pakket.handler.createSetSlotItemPacket(126, 0, slot, item)
                for (player in viewers.values) {
                    if (slot > type.lastIndex && item == null) {
                        val playerItemIndex = playerSlotFromMenuSlot(slot, this)
                        val playerItem = player.player.inventory.getItem(playerItemIndex)
                        val packet = Pakket.handler.createSetSlotItemPacket(126, 0, slot, playerItem)
                        player.player.sendPacket(packet, true)
                    } else {
                        player.player.sendPacket(packet, true)
                    }
                }
            }
        }
    }

    suspend fun changeItems(items: Map<Int, ItemStack?>) {
        logicMutex.withLock {
            for ((slot, item) in items) {
                if (item == null) {
                    content.remove(slot)
                } else {
                    addItem(slot, item)
                }
            }
        }
        for ((_, viewer) in viewers) {
            InventoryModule.updateInventoryContent(this, viewer)
        }
    }

    suspend fun setItems(items: Map<Int, ItemStack>) {
        logicMutex.withLock {
            this.content.clear()
            for ((slot, item) in items) {
                this.content[slot] = item
            }

            for (viewer in viewers.values) {
                InventoryModule.updateInventoryContent(this, viewer)
            }
        }
    }

    fun updateItems(player: Player) {
        val viewer = viewers[player.uniqueId] ?: return
        InventoryModule.updateInventoryContent(this, viewer)
    }

    fun updateItems() {
        for ((_, viewer) in viewers) {
            InventoryModule.updateInventoryContent(this, viewer)
        }
    }

    override fun clone(): PacketInventory {
        val inv = PacketInventory(title, type)
        content.forEach { (key, value) -> inv.content[key] = value.clone() }
        return inv
    }

    fun close(player: Player) {
        player.sendPacket(Pakket.handler.closeWindowPacket(126))
    }
}