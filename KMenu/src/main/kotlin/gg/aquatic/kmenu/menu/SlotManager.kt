package gg.aquatic.kmenu.menu

class SlotManager {
    // Single source of truth for components
    val components = mutableMapOf<String, MenuComponent>()
    // Tracks which component ID is currently VISIBLE in which slot
    private val slotOwners = mutableMapOf<Int, String>()

    fun addComponent(component: MenuComponent) {
        components[component.id] = component
    }

    /**
     * Removes the component and returns a list of slots that were owned by it
     * so the Menu knows which ones to recalculate.
     */
    fun removeComponent(id: String): Set<Int> {
        components.remove(id)
        val affectedSlots = mutableSetOf<Int>()
        slotOwners.entries.removeIf { (slot, ownerId) ->
            if (ownerId == id) {
                affectedSlots.add(slot)
                true
            } else false
        }
        return affectedSlots
    }

    fun getTopComponentForSlot(slot: Int): MenuComponent? {
        var top: MenuComponent? = null
        for (component in components.values) {
            if (component.slots.contains(slot)) {
                if (top == null || component.priority > top.priority) {
                    top = component
                }
            }
        }
        return top
    }

    fun setOwner(slot: Int, id: String) {
        slotOwners[slot] = id
    }

    fun getOwner(slot: Int): String? = slotOwners[slot]

    fun removeOwner(slot: Int) {
        slotOwners.remove(slot)
    }

    fun getAllOccupiedSlots(): Set<Int> = components.values.flatMap { it.slots }.toSet()
    fun getCurrentlyRenderedSlots(): Set<Int> = slotOwners.keys.toSet()
}
