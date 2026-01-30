package gg.aquatic.kmenu.menu.settings

import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionHandle
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.Button
import gg.aquatic.replace.PlaceholderContext
import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.StackedItemImpl
import org.bukkit.entity.Player

class ButtonSettings(
    val id: String,
    val item: StackedItem?,
    val slots: Collection<Int>,
    val viewRequirements: Collection<ConditionHandle<Player>>,
    val click: ClickSettings?,
    val priority: Int,
    val updateEvery: Int,
    val failComponent: IButtonSettings?
) : IButtonSettings {

    override fun create(updater: PlaceholderContext<Menu>, click: suspend (AsyncPacketInventoryInteractEvent) -> Unit): Button {
        return Button(
            id,
            item?.getItem(),
            slots,
            priority,
            updateEvery,
            failComponent?.create(updater),
            { menu: Menu ->
                if (menu is PrivateMenu) {
                    viewRequirements.checkConditions(menu.player)
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