package gg.aquatic.kmenu.menu.settings

import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionHandle
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.AnimatedButton
import gg.aquatic.replace.placeholder.PlaceholderContext
import org.bukkit.entity.Player
import java.util.*

class AnimatedButtonSettings(
    val id: String,
    val frames: TreeMap<Int, IButtonSettings>,
    val viewRequirements: Collection<ConditionHandle<Player>>,
    val click: ClickSettings?,
    val priority: Int,
    val updateEvery: Int,
    val failComponent: IButtonSettings?
) : IButtonSettings {
    override fun create(updater: PlaceholderContext<Menu>, click: suspend (AsyncPacketInventoryInteractEvent) -> Unit): AnimatedButton {
        return AnimatedButton(
            id,
            TreeMap(frames.mapValues { it.value.create(updater) }),
            priority,
            updateEvery,
            failComponent?.create(updater),
            { menu ->
                if (menu is PrivateMenu) {
                    viewRequirements.checkConditions(menu.player)
                } else true
            },
            { e ->
                click(e)
                this.click?.handleClick(e) { _, s -> updater.createItem(e.inventory as Menu, s).latestState.value }
            }
        )
    }
}