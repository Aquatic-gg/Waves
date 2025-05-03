package gg.aquatic.waves.inventory.event

import gg.aquatic.waves.inventory.ButtonType
import gg.aquatic.waves.inventory.InventoryViewer
import gg.aquatic.waves.inventory.PacketInventory
import gg.aquatic.waves.api.event.AquaticEvent
import org.bukkit.inventory.ItemStack

class AsyncPacketInventoryInteractEvent(
    val viewer: InventoryViewer,
    val inventory: PacketInventory,
    val slot: Int,
    val buttonType: ButtonType,
    val cursor: ItemStack?,
    val slots: Map<Int,ItemStack>
): AquaticEvent(true)