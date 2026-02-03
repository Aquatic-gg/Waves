package gg.aquatic.kmenu.menu.settings

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.AnimatedButton
import gg.aquatic.replace.placeholder.PlaceholderContext
import org.bukkit.entity.Player
import java.util.*

/** Settings for an animated button component. */
class AnimatedButtonSettings(
    val id: String,
    val frames: TreeMap<Int, IButtonSettings>,
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

    override fun create(updater: PlaceholderContext<Menu>, click: suspend (AsyncPacketInventoryInteractEvent) -> Unit): AnimatedButton {
        return AnimatedButton(
            id,
            TreeMap(frames.mapValues { it.value.create(updater) }),
            priority,
            updateEvery,
            failComponent?.create(updater),
            { menu ->
                if (menu is PrivateMenu) {
                    checkViewRequirements(menu.player)
                } else true
            },
            { e ->
                click(e)
                this.click?.handleClick(e) { _, s -> updater.createItem(e.inventory as Menu, s).latestState.value }
            }
        )
    }
}
