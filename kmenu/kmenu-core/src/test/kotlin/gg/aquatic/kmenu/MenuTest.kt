package gg.aquatic.kmenu

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import gg.aquatic.kmenu.menu.SlotManager
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MenuTest {

    class TestComponent(
        override val id: String,
        override val slots: Collection<Int>,
        override val priority: Int
    ) : MenuComponent() {
        override val onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = {}
        override suspend fun itemstack(menu: Menu): ItemStack? = null
        override suspend fun tick(menu: Menu) {}
    }

    @Test
    fun `test priority slot ownership logic`() {
        // We test SlotManager directly because it holds the logic previously in Menu
        val manager = SlotManager()

        val lowPriority = TestComponent("low", listOf(0), 1)
        val highPriority = TestComponent("high", listOf(0), 10)

        manager.addComponent(lowPriority)
        manager.setOwner(0, "low") // Simulate initial render

        manager.addComponent(highPriority)
        // Simulate priority check
        val winner = manager.getTopComponentForSlot(0)
        assertEquals("high", winner?.id, "Higher priority component should be identified as top")

        // Update ownership as Menu would do
        manager.setOwner(0, winner!!.id)

        manager.removeComponent("high")
        val newWinner = manager.getTopComponentForSlot(0)
        assertEquals("low", newWinner?.id, "Slot should revert to lower priority component after removal")
    }
}
