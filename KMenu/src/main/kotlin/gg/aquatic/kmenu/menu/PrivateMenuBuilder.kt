package gg.aquatic.kmenu.menu

import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.menu.component.Button
import gg.aquatic.kmenu.menu.util.ScrollingButton
import gg.aquatic.kmenu.privateMenu
import gg.aquatic.replace.placeholder.PlaceholderContext
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PrivateMenuBuilder(
    val player: Player,
    var title: Component,
    var type: InventoryType = InventoryType.GENERIC9X6
) {
    var cancelBukkitInteractions: Boolean = true
    private val components = mutableListOf<MenuComponent>()

    fun button(id: String, slot: Int, block: ButtonBuilder.() -> Unit) {
        button(id, listOf(slot), block)
    }

    fun button(id: String, slots: Collection<Int>, block: ButtonBuilder.() -> Unit) {
        val builder = ButtonBuilder(id, slots)
        builder.block()
        components.add(builder.build())
    }

    private val deferredActions = mutableListOf<suspend (PrivateMenu) -> Unit>()

    fun <A> scrollingButton(
        slots: Collection<Int>,
        scrolls: List<ScrollingButton.Scroll<A>>,
        onScroll: (A) -> Unit = {}
    ) {
        // We'll use a deferred registration since ScrollingButton.create is suspend
        // and addComponent requires a live menu
        deferredActions.add { menu ->
            ScrollingButton.create(menu, slots, scrolls, PlaceholderContext.privateMenu(), onScroll)
        }
    }

    suspend fun build(): PrivateMenu {
        val menu = PrivateMenu(title, type, player, cancelBukkitInteractions)
        components.forEach { menu.addComponent(it) }
        deferredActions.forEach { it(menu) }
        return menu
    }
}

class ButtonBuilder(val id: String, val slots: Collection<Int>) {
    var item: ItemStack? = null
    var priority: Int = 1
    var updateEvery: Int = 1000
    var textUpdater: PlaceholderContext<Menu>? = null
    var onClick: suspend (gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent) -> Unit = {}

    fun onClick(block: suspend (gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent) -> Unit) {
        this.onClick = block
    }

    fun build(): Button {
        return Button(
            id, item, slots, priority, updateEvery, null,
            textUpdater = textUpdater ?: PlaceholderContext.privateMenu(),
            onClick = onClick
        )
    }
}

suspend fun Player.createMenu(
    title: Component,
    type: InventoryType = InventoryType.GENERIC9X6,
    block: PrivateMenuBuilder.() -> Unit
): PrivateMenu {
    val builder = PrivateMenuBuilder(this, title, type)
    builder.block()
    return builder.build()
}