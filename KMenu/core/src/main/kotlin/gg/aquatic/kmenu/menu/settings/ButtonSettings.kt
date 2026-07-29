package gg.aquatic.kmenu.menu.settings

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.Button
import gg.aquatic.replace.PlaceholderContext
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

typealias ViewRequirement = suspend (Player) -> Boolean

/** Settings for a static button component. */
class ButtonSettings(
    val id: String,
    val item: ItemStack?,
    val slots: Collection<Int>,
    val viewRequirements: Collection<ViewRequirement>,
    val click: ClickSettings?,
    val priority: Int,
    val updateEvery: Int,
    val failComponent: IButtonSettings?
) : IButtonSettings {

    private suspend fun checkViewRequirements(player: Player): Boolean {
        for (requirement in viewRequirements) {
            if (!requirement(player)) return false
        }
        return true
    }

    override fun create(updater: PlaceholderContext<Menu>, click: suspend (AsyncPacketInventoryInteractEvent) -> Unit): Button {
        return Button(
            id,
            item,
            slots,
            priority,
            updateEvery,
            failComponent?.create(updater),
            { menu: Menu ->
                if (menu is PrivateMenu) {
                    checkViewRequirements(menu.player)
                } else
                    true
            },
            updater,
            { e ->
                click(e)
                this.click?.handleClick(e) { _, s -> updater.createItem(e.inventory as Menu, s).latestState.value }
            }
        )
    }
}
