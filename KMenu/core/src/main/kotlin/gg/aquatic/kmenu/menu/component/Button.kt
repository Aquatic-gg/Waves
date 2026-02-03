package gg.aquatic.kmenu.menu.component

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import gg.aquatic.replace.PlaceholderContext
import org.bukkit.inventory.ItemStack
import kotlin.properties.Delegates

/** A static menu button with optional placeholders and view requirements. */
class Button(
    override val id: String,
    private var itemstack: ItemStack?,
    slots: Collection<Int>,
    priority: Int,
    val updateEvery: Int,
    failComponent: MenuComponent? = null,
    viewRequirements: suspend (Menu) -> Boolean = { true },
    var textUpdater: PlaceholderContext<Menu>,
    onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = { _ -> }
) : MenuComponent() {

    override var priority: Int = priority
        private set
        get() {
            if (currentComponent == null) {
                return field
            }
            return currentComponent?.priority ?: field
        }
    override var slots: Collection<Int> = slots
        private set
        get() {
            if (currentComponent == null) {
                return field
            }
            return currentComponent?.slots ?: listOf()
        }
    override var onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = onClick
        get() {
            if (currentComponent == null) {
                return field
            }
            return currentComponent?.onClick ?: { _ -> }
        }

    var viewRequirements: suspend (Menu) -> Boolean = viewRequirements
        private set
    var failComponent: MenuComponent? = failComponent
        private set

    private var currentComponent: MenuComponent? = null

    private var currentItem: PlaceholderContext<Menu>.ItemStackItem? = null

    var itemStack by Delegates.observable(itemstack) { _, old, new ->
        if (old == new) return@observable
        currentItem = null
    }

    override suspend fun itemstack(menu: Menu): ItemStack? {
        if (!viewRequirements(menu)) {
            if (currentComponent != failComponent) {
                currentComponent = failComponent
            }
            return currentComponent?.itemstack(menu)
        }
        // Ensure we reset currentComponent if requirements are met again
        currentComponent = null

        val current = currentItem
        if (current == null) {
            val item = itemstack?.clone() ?: return null
            val updated = textUpdater.createItem(menu, item)
            currentItem = updated
            return updated.latestState.value
        } else {
            return current.tryUpdate(menu).value
        }
    }

    private var tick = 0
    override suspend fun tick(menu: Menu) {
        if (tick >= updateEvery) {
            tick = 0
            menu.updateComponent(this)
        }
        tick++
    }
}
