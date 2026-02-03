package gg.aquatic.kmenu.menu.util

import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.Button
import gg.aquatic.replace.placeholder.PlaceholderContext
import org.bukkit.inventory.ItemStack
import java.util.*

/** A button that cycles through a list of entries on click. */
class ScrollingButton<A> private constructor(
    val menu: PrivateMenu,
    val slots: Collection<Int>,
    val scroll: List<Scroll<A>>,
    val placeholderContext: PlaceholderContext<Menu>,
    val onScroll: (A) -> Unit,
    val onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit = { _ -> }
) {

    private val uuid = UUID.randomUUID()

    var currentIndex = 0
        private set
    val currentButton: Button = Button(
        "scroll:$uuid",
        scroll[currentIndex].item(),
        slots,
        1,
        1000,
        null,
        textUpdater = placeholderContext,
        onClick = { e ->
            onClick(e)
            if (e.buttonType == ButtonType.RIGHT) {
                scroll(forward = false)
            } else {
                scroll(forward = true)
            }
        }
    )
    val entry: A
        get() {
            return scroll[currentIndex].entry
        }

    suspend fun scroll(forward: Boolean = true) {
        val nextIndex = if (forward) {
            (currentIndex + 1) % scroll.size
        } else {
            (currentIndex - 1 + scroll.size) % scroll.size
        }
        setIndex(nextIndex)
    }

    suspend fun setIndex(index: Int) {
        if (index == currentIndex && currentButton.itemStack != null) return

        currentIndex = index
        val data = scroll[currentIndex]
        onScroll(data.entry)

        currentButton.itemStack = data.item()
        menu.updateComponent(currentButton)
    }

    companion object {
        suspend fun <A> create(
            menu: PrivateMenu,
            slots: Collection<Int>,
            scroll: List<Scroll<A>>,
            placeholderContext: PlaceholderContext<Menu>,
            onScroll: (A) -> Unit
        ): ScrollingButton<A> {
            val scrollingButton = ScrollingButton(menu, slots, scroll, placeholderContext, onScroll)
            menu.addComponent(scrollingButton.currentButton)
            return scrollingButton
        }
    }

    class Scroll<A>(
        val entry: A,
        val item: () -> ItemStack
    )
}
