package gg.aquatic.kmenu.menu

import gg.aquatic.kmenu.coroutine.KMenuCtx
import gg.aquatic.kmenu.inventory.InventoryModule
import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.inventory.PacketInventory
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

open class Menu(
    title: Component,
    type: InventoryType,
    val cancelBukkitInteractions: Boolean
): PacketInventory(title,type) {


    private val slotManager = SlotManager()
    private val componentStates = hashMapOf<String, ComponentState>()

    protected val components get() = slotManager.components

    open suspend fun open(player: Player) = withContext(KMenuCtx) {
        InventoryModule.openMenu(player, this@Menu)
    }

    suspend fun addComponent(component: MenuComponent) {
        components[component.id] = component
        updateComponent(component)
    }

    suspend fun removeComponent(component: MenuComponent) {
        removeComponent(component.id)
    }

    suspend fun removeComponent(id: String) {
        componentStates.remove(id)
        val affectedSlots = slotManager.removeComponent(id)

        affectedSlots.forEach { slot ->
            recalculateSlot(slot)
        }
    }

    private suspend fun recalculateSlot(slot: Int) {
        val topComponent = slotManager.getTopComponentForSlot(slot)
        if (topComponent != null) {
            slotManager.setOwner(slot, topComponent.id)
            this.setItem(slot, topComponent.itemstack(this))
        } else {
            slotManager.removeOwner(slot)
            this.setItem(slot, null)
        }
    }

    suspend fun updateComponents() {
        val allOccupied = slotManager.getAllOccupiedSlots()
        val currentlyRendered = slotManager.getCurrentlyRenderedSlots()

        // Clean up slots that no longer have ANY component
        (currentlyRendered - allOccupied).forEach { slot ->
            slotManager.removeOwner(slot)
            this.setItem(slot, null)
        }

        for (component in components.values) {
            renderComponent(component)
        }
    }

    suspend fun updateComponent(component: MenuComponent) {
        renderComponent(component)
    }

    suspend fun updateComponent(id: String) {
        val component = components[id] ?: return
        renderComponent(component)
    }

    private suspend fun renderComponent(component: MenuComponent) {
        val item = component.itemstack(this)
        val newState = ComponentState(component.slots.toList(), item)
        val oldState = componentStates[component.id]

        if (oldState != null && oldState.slots == newState.slots && isSameItem(oldState.itemStack, item)) {
            return
        }

        componentStates[component.id] = newState

        // Clean up old slots if the component moved
        oldState?.slots?.forEach { slot ->
            if (!component.slots.contains(slot) && slotManager.getOwner(slot) == component.id) {
                recalculateSlot(slot)
            }
        }

        // Render slots where this component has priority
        for (slot in component.slots) {
            val ownerId = slotManager.getOwner(slot)
            val currentOwner = ownerId?.let { components[it] }

            if (currentOwner == null || currentOwner.priority <= component.priority) {
                slotManager.setOwner(slot, component.id)
                setItem(slot, item)
            }
        }
    }

    private fun isSameItem(a: ItemStack?, b: ItemStack?): Boolean {
        if (a === b) return true
        if (a == null || b == null) return false
        if (a.amount != b.amount || a.type != b.type) return false
        return a.isSimilar(b)
    }

    internal suspend fun tick() {
        for (value in components.values) {
            value.tick(this)
        }
    }

    internal suspend fun onInteract(event: AsyncPacketInventoryInteractEvent) {
        val componentId = slotManager.getOwner(event.slot) ?: return
        val component = components[componentId] ?: return

        component.onClick.invoke(event)
    }

    class ComponentState(
        val slots: Collection<Int>,
        val itemStack: ItemStack?
    )
}