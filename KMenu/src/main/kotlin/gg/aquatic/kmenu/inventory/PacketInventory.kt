package gg.aquatic.kmenu.inventory

import gg.aquatic.kmenu.inventory.InventoryHandler.playerSlotFromMenuSlot
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.sendPacket
import gg.aquatic.snapshotmap.SnapshotMap
import gg.aquatic.snapshotmap.SuspendingSnapshotMap
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

open class PacketInventory(
    title: Component,
    val type: InventoryType
) : Cloneable {

    val viewers = SuspendingSnapshotMap<UUID, InventoryViewer>()
    val content = SnapshotMap<Int, ItemStack>()

    var title: Component = title
        private set

    suspend fun updateTitle(newTitle: Component) {
        this.title = newTitle
        this.inventoryOpenPacket = createOpenPacket()

        viewers.forEachSuspended { _, viewer ->
            val player = viewer.player
            player.sendPacket(inventoryOpenPacket)
            InventoryHandler.updateInventoryContent(this, viewer)
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

        if (item == null) content.remove(slot) else content[slot] = item

        if (update) {
            if (slot > type.size + 36) return
            if (item == null) {
                content.remove(slot)
            } else {
                addItem(slot, item)
            }
            val packet = Pakket.handler.createSetSlotItemPacket(126, 0, slot, item)
            viewers.forEachSuspended { _, player ->
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

    suspend fun changeItems(items: Map<Int, ItemStack?>) {
        for ((slot, item) in items) {
            if (item == null) {
                content.remove(slot)
            } else {
                addItem(slot, item)
            }
        }

        viewers.forEachSuspended { _, viewer ->
            InventoryHandler.updateInventoryContent(this, viewer)
        }
    }

    suspend fun setItems(items: Map<Int, ItemStack>) {
        this.content.clear()
        for ((slot, item) in items) {
            this.content[slot] = item
        }

        viewers.forEachSuspended { _, viewer ->
            InventoryHandler.updateInventoryContent(this, viewer)
        }
    }

    fun updateItems(player: Player) {
        val viewer = viewers[player.uniqueId] ?: return
        InventoryHandler.updateInventoryContent(this, viewer)
    }

    suspend fun updateItems() {
        viewers.forEachSuspended { _, viewer ->
            InventoryHandler.updateInventoryContent(this, viewer)
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