package gg.aquatic.kmenu.menu.component

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import org.bukkit.inventory.ItemStack
import java.util.TreeMap

class AnimatedButton(
    override val id: String,
    val frames: TreeMap<Int,MenuComponent>,
    priority: Int,
    val updateEvery: Int,
    failComponent: MenuComponent?,
    viewRequirements: suspend (Menu) -> Boolean = { true },
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
    override var slots: Collection<Int> = listOf()
        private set
        get() {
            if (currentComponent == null) {
                return currentFrame.slots
            }
            return currentComponent?.slots ?: currentFrame.slots
        }
    override var onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = onClick
        private set
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

    private var currentFrame = frames.firstEntry().value
    override suspend fun itemstack(menu: Menu): ItemStack? {
        if (!viewRequirements(menu)) {
            currentComponent = failComponent
            return currentComponent?.itemstack(menu)
        }
        return currentFrame.itemstack(menu)
    }

    private var animationTick = 0
    private var tick = 0
    override suspend fun tick(menu: Menu) {
        if (animationTick >= frames.lastKey()) {
            animationTick = 0
        }
        if (frames.containsKey(animationTick)) {
            currentFrame = frames[animationTick]
        }
        if (tick >= updateEvery) {
            tick = 0
            menu.updateComponent(this)
        }
        animationTick++
        tick++
    }
}