package gg.aquatic.waves.menu.component

import gg.aquatic.waves.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.waves.menu.AquaticMenu
import gg.aquatic.waves.menu.MenuComponent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack

class Button(
    override val id: String,
    itemstack: ItemStack?,
    slots: Collection<Int>,
    priority: Int,
    val updateEvery: Int,
    failComponent: MenuComponent?,
    viewRequirements: (AquaticMenu) -> Boolean = { true },
    textUpdater: (String, AquaticMenu) -> String = { s, _ -> s },
    onClick: (AsyncPacketInventoryInteractEvent) -> Unit = { _ -> }
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
    override var onClick: (AsyncPacketInventoryInteractEvent) -> Unit = onClick
        private set
        get() {
            if (currentComponent == null) {
                return field
            }
            return currentComponent?.onClick ?: { _ -> }
        }

    var viewRequirements: (AquaticMenu) -> Boolean = viewRequirements
        private set
    var textUpdater: (String, AquaticMenu) -> String = textUpdater
        private set
    var failComponent: MenuComponent? = failComponent
        private set

    private var currentComponent: MenuComponent? = null

    private var itemstack: ItemStack? = itemstack

    override fun itemstack(menu: AquaticMenu): ItemStack? {
        if (!viewRequirements(menu)) {
            currentComponent = failComponent
            return currentComponent?.itemstack(menu)
        }
        val iS = itemstack?.clone()
        val meta = iS?.itemMeta

        if (meta != null) {
            meta.displayName()?.let { comp ->
                meta.displayName(MiniMessage.miniMessage().deserialize(textUpdater(MiniMessage.miniMessage().serialize(comp), menu))
                    .decoration(TextDecoration.ITALIC, false))
            }

            meta.lore()?.let { lore ->
                meta.lore(lore.map {
                    MiniMessage.miniMessage().deserialize(textUpdater(MiniMessage.miniMessage().serialize(it), menu))
                        .decoration(TextDecoration.ITALIC, false)
                })
            }

            iS.itemMeta = meta
        }
        return iS
    }

    private var tick = 0
    override fun tick(menu: AquaticMenu) {
        if (tick >= updateEvery) {
            tick = 0
            menu.updateComponent(this)
        }
        tick++
    }

    fun modifyButton(menu: AquaticMenu) = ButtonUpdate(menu)

    inner class ButtonUpdate(val menu: AquaticMenu) {
        private var priority = this@Button.priority
        private var slots = this@Button.slots
        private var onClick = this@Button.onClick
        private var itemstack = this@Button.itemstack
        private var viewRequirements = this@Button.viewRequirements
        private var failComponent = this@Button.failComponent

        fun priority(priority: Int): ButtonUpdate {
            this.priority(priority)
            return this
        }

        fun slots(slots: Collection<Int>): ButtonUpdate {
            this.slots(slots)
            return this
        }

        fun onClick(onClick: (AsyncPacketInventoryInteractEvent) -> Unit): ButtonUpdate {
            this.onClick(onClick)
            return this
        }

        fun itemstack(itemstack: ItemStack): ButtonUpdate {
            this.itemstack(itemstack)
            return this
        }

        fun viewRequirements(viewRequirements: Collection<(AquaticMenu) -> Boolean>): ButtonUpdate {
            this.viewRequirements(viewRequirements)
            return this
        }

        fun failComponent(failComponent: MenuComponent): ButtonUpdate {
            this.failComponent(failComponent)
            return this
        }

        fun finish(): Button {
            this@Button.priority = priority
            this@Button.slots = slots
            this@Button.onClick = onClick
            this@Button.itemstack = itemstack
            this@Button.viewRequirements = viewRequirements
            this@Button.failComponent = failComponent

            menu.updateComponent(this@Button)
            return this@Button
        }
    }

}