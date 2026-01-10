package gg.aquatic.kmenu.inventory.event

import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.inventory.InventoryViewer
import gg.aquatic.kmenu.inventory.PacketInventory
import org.bukkit.inventory.ItemStack

class AsyncPacketInventoryInteractEvent(
    val viewer: InventoryViewer,
    val inventory: PacketInventory,
    val slot: Int,
    val buttonType: ButtonType,
    val cursor: ItemStack?,
    val slots: Map<Int,ItemStack>
)