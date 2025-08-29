package gg.aquatic.waves.menu

import gg.aquatic.waves.inventory.InventoryManager
import gg.aquatic.waves.inventory.InventoryType
import gg.aquatic.waves.inventory.PacketInventory
import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

open class AquaticMenu(
    title: Component,
    type: InventoryType,
    val cancelBukkitInteractions: Boolean
): PacketInventory(title,type) {

    val components = ConcurrentHashMap<String,MenuComponent>()
    val renderedComponents = ConcurrentHashMap<Int,String>()
    val componentStates = ConcurrentHashMap<String,ComponentState>()

    open fun open(player: Player) {
        InventoryManager.openMenu(player, this)
    }

    fun addComponent(component: MenuComponent) {
        components[component.id] = component
        updateComponent(component)
    }

    fun removeComponent(component: MenuComponent) {
        components.remove(component.id) ?: return
        updateComponents()
    }

    fun updateComponents() {
        val renderedToRemove = mutableListOf<Int>()
        for ((slot, component) in renderedComponents) {
            if (!components.containsKey(component)) {
                renderedToRemove.add(slot)
                continue
            }
        }
        for (slot in renderedToRemove) {
            renderedComponents.remove(slot)
            this.setItem(slot,null)
        }
        for ((_, component) in components) {
            updateComponent(component)
        }
    }

    fun updateComponent(component: MenuComponent) {
        if (!components.containsKey(component.id)) {
            updateComponents()
            return
        }
        val item = component.itemstack(this) ?: ItemStack(Material.AIR)
        val state = ComponentState(component.slots, item)

        val previousComponentState = componentStates[component.id]
        val remainingSlots = previousComponentState?.slots?.subtract(state.slots.toSet())
        componentStates[component.id] = state
        for (slot in component.slots) {
            val previousComponentId = renderedComponents[slot]
            val previousComponent = previousComponentId?.let { components[it] }

            val currentItem = this.content[slot]
            if (previousComponent == null || previousComponentState == null) {
                renderedComponents[slot] = component.id
                if (currentItem != null) {
                    if (currentItem.isSimilar(item)) {
                        continue
                    }
                }
                this.setItem(slot,item)
                continue
            }
            if (previousComponent.priority <= component.priority) {
                renderedComponents[slot] = component.id
                if (currentItem != null) {
                    if (currentItem.isSimilar(item)) {
                        continue
                    }
                }
                this.setItem(slot,item)
            }
        }
        remainingSlots ?: return
        for (slot in remainingSlots) {
            var comp: Pair<MenuComponent,ComponentState>? = null
            for ((id, value) in components) {
                if (!value.slots.contains(slot)) continue
                if (comp == null || comp.first.priority < value.priority) {
                    val compState = componentStates[id] ?: continue
                    comp = value to compState
                }
            }
            if (comp == null) {
                renderedComponents.remove(slot)
                this.setItem(slot,null)
                continue
            }
            val i = comp.second.itemStack
            this.setItem(slot,i)
            renderedComponents[slot] = comp.first.id
        }
    }

    internal fun tick() {
        for (value in components.values) {
            value.tick(this)
        }
    }

    internal fun onInteract(event: AsyncPacketInventoryInteractEvent) {
        renderedComponents[event.slot]?.let {
            components[it]?.onClick?.invoke(event)
        }
    }

    class ComponentState(
        val slots: Collection<Int>,
        val itemStack: ItemStack?
    )
}