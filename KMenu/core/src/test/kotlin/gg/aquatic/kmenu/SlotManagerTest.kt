package gg.aquatic.kmenu

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import gg.aquatic.kmenu.menu.SlotManager
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SlotManagerTest {

    // A lightweight mock that doesn't trigger Bukkit loading
    class MockComponent(
        override val id: String,
        override val slots: Collection<Int>,
        override val priority: Int
    ) : MenuComponent() {
        override val onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = {}
        override suspend fun itemstack(menu: Menu): ItemStack? = null
        override suspend fun tick(menu: Menu) {}
    }

    @Test
    fun `test priority conflict resolution`() {
        val manager = SlotManager()
        val low = MockComponent("low", listOf(0), 1)
        val high = MockComponent("high", listOf(0), 10)

        manager.addComponent(low)
        manager.addComponent(high)

        val winner = manager.getTopComponentForSlot(0)
        assertEquals("high", winner?.id)
    }

    @Test
    fun `test removal and affected slots`() {
        val manager = SlotManager()
        val comp = MockComponent("test", listOf(0, 1, 2), 1)
        
        manager.addComponent(comp)
        manager.setOwner(0, "test")
        manager.setOwner(1, "test")

        val affected = manager.removeComponent("test")
        
        assertEquals(setOf(0, 1), affected)
        assertNull(manager.components["test"])
    }
}
