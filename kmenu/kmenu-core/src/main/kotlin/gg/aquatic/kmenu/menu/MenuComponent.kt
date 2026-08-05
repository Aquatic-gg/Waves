package gg.aquatic.kmenu.menu

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import org.bukkit.inventory.ItemStack

/** A renderable menu component with slots, priority, and click handling. */
abstract class MenuComponent {

    abstract val id: String
    abstract val priority: Int
    abstract val slots: Collection<Int>
    abstract val onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit

    abstract suspend fun itemstack(menu: Menu): ItemStack?

    abstract suspend fun tick(menu: Menu)
}
