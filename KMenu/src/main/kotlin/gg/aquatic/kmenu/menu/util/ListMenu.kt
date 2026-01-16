package gg.aquatic.kmenu.menu.util

import gg.aquatic.kmenu.coroutine.KMenuCtx
import gg.aquatic.kmenu.inventory.ButtonType
import gg.aquatic.kmenu.inventory.InventoryType
import gg.aquatic.kmenu.inventory.event.AsyncPacketInventoryInteractEvent
import gg.aquatic.kmenu.menu.Menu
import gg.aquatic.kmenu.menu.MenuComponent
import gg.aquatic.kmenu.menu.PrivateMenu
import gg.aquatic.kmenu.menu.component.Button
import gg.aquatic.kmenu.menu.settings.IButtonSettings
import gg.aquatic.kmenu.privateMenu
import gg.aquatic.replace.PlaceholderContext
import gg.aquatic.replace.toPlain
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.properties.Delegates

abstract class ListMenu<T>(
    title: Component, type: InventoryType, player: Player,
    entries: Collection<Entry<T>>,
    defaultSorting: Sorting<T>,
    val entrySlots: Collection<Int>,
    val onEntryButtonGenerate: (ListMenu<T>, Entry<T>, Button) -> Button = { _, _, b -> b }
) :
    PrivateMenu(
        title, type, player,
        true
    ) {

    val placeholderContext = PlaceholderContext.privateMenu()

    var page = 0
        private set

    val pageIndex: Int
        get() {
            return page * entrySlots.size
        }

    var search: String? by Delegates.observable(null) { _, old, new ->
        if (old == new) return@observable
        requestRefresh()
    }

    private var refreshJob: Job? = null
    fun requestRefresh() {
        val previousJob = refreshJob
        refreshJob = KMenuCtx.launch {
            previousJob?.cancelAndJoin()

            refreshEntries()
            refreshButtons()
        }
    }

    var nextPageButton: MenuComponent? = null
        private set
    var previousPageButton: MenuComponent? = null
        private set

    var sorting: Sorting<T> by Delegates.observable(defaultSorting) { _, old, new ->
        if (old == new) return@observable
        refreshEntries(new)

        KMenuCtx.launch {
            refreshButtons()
        }
    }

    val entryButtons = ArrayList<MenuComponent>()

    private val filters = HashMap<String, (T) -> Boolean>()

    var entries by Delegates.observable(entries) { _, old, new ->
        if (old == new) return@observable
        val previousPage = page
        refreshEntries()
        page = if (pageIndex > filteredEntries.size) {
            max(0, previousPage - 1)
        } else previousPage
    }

    var filteredEntries = entries
        private set

    override suspend fun open() {
        super.open()
        refreshEntries()
        refreshButtons()
    }

    override suspend fun open(player: Player) {
        open()
    }

    suspend fun injectPreviousButton(settings: IButtonSettings) {
        previousPageButton = settings.create(placeholderContext) { e ->
            if (e.buttonType != ButtonType.LEFT) return@create
            if (page == 0) {
                return@create
            }
            page--
            onPageChange(page + 1, page)
            refreshButtons()
        }
        refreshPageButtons()
    }

    suspend fun injectNextButton(settings: IButtonSettings) {
        nextPageButton = settings.create(placeholderContext) { e ->
            if (e.buttonType != ButtonType.LEFT) return@create
            if ((page + 1) * entrySlots.size > filteredEntries.size) return@create
            page++
            onPageChange(page - 1, page)
            refreshButtons()
        }
        refreshPageButtons()
    }

    fun setFilter(id: String, filter: (T) -> Boolean) {
        filters[id] = filter
        refreshEntries()
    }

    fun clearFilters() {
        filters.clear()
        refreshEntries()
    }

    fun hasFilter(id: String) = filters.containsKey(id)
    fun removeFilter(id: String) {
        filters.remove(id)
        refreshEntries()
    }

    fun refreshEntries(sorting: Sorting<T> = this.sorting) {
        page = 0
        filteredEntries = sorting.sort(entries.filter {
            for ((_, filter) in filters) {
                if (!filter(it.value)) {
                    return@filter false
                }
            }

            val search = this.search
            if (search != null) {
                val item = it.itemVisual()
                val meta = item.itemMeta

                return@filter (meta?.displayName()?.toPlain() ?: item.type.toString().lowercase()
                    .replace("_", " "))
                    .contains(search, true)
            }

            true
        })
    }

    suspend fun refreshButtons() {
        refreshEntryButtons()
        refreshPageButtons()
        updateComponents()
    }


    open fun onPageChange(from: Int, to: Int) {}

    suspend fun refreshPageButtons() {
        if (page == 0) {
            previousPageButton?.let { removeComponent(it) }
        } else if ("prev-page" !in components) {
            previousPageButton?.let { addComponent(it) }
        }
        if ((page + 1) * entrySlots.size > filteredEntries.size) {
            nextPageButton?.let { removeComponent(it) }
        } else if ("next-page" !in components) {
            nextPageButton?.let { addComponent(it) }
        }
    }

    suspend fun refreshEntryButtons() {
        val pageIndex = pageIndex

        // Track which components we are actually using this time
        val newButtons = ArrayList<MenuComponent>()

        for ((index, slot) in entrySlots.withIndex()) {
            val entryIndex = index + pageIndex
            val entry = filteredEntries.elementAtOrNull(entryIndex) ?: break

            // Reuse existing button if possible to avoid packet spam
            val existing = entryButtons.getOrNull(index) as? Button
            val button = if (existing != null && existing.id == "entry:$slot") {
                existing.itemStack = entry.itemVisual()
                // Update onClick to match new entry data
                existing.onClick = entry.onClick
                existing
            } else {
                entry.createButton(slot)
            }

            newButtons.add(onEntryButtonGenerate(this, entry, button))
        }

        // Remove only components that are no longer present
        val toRemove = entryButtons - newButtons.toSet()
        toRemove.forEach { removeComponent(it) }

        entryButtons.clear()
        entryButtons.addAll(newButtons)

        // Add only the ones that aren't in the menu yet
        for (btn in entryButtons) {
            if (btn.id !in components) {
                addComponent(btn)
            } else {
                updateComponent(btn)
            }
        }
    }

    interface Sorting<T> {
        fun sort(entries: Collection<Entry<T>>): Collection<Entry<T>>
        val id: String
        val index: Int

        companion object {
            fun <T> create(
                id: String,
                sorter: (Collection<Entry<T>>) -> Collection<Entry<T>>,
                index: Int
            ): Sorting<T> {
                return SimpleSorting(id, sorter, index)
            }

            fun <T> empty(
            ): Sorting<T> {
                return SimpleSorting("empty", { l -> l }, 0)
            }
        }
    }

    open class SimpleSorting<T>(
        override val id: String, private val sorter: (Collection<Entry<T>>) -> Collection<Entry<T>>,
        override val index: Int
    ) : Sorting<T> {
        override fun sort(entries: Collection<Entry<T>>): Collection<Entry<T>> {
            return sorter(entries)
        }
    }

    class Entry<T>(
        val value: T,
        val itemVisual: () -> ItemStack,
        val placeholderContext: PlaceholderContext<Menu>,
        val onClick: suspend (AsyncPacketInventoryInteractEvent) -> Unit,
        val createButton: (slot: Int) -> Button = { slot ->
            Button(
                "entry:$slot",
                itemVisual(),
                listOf(slot),
                1,
                10,
                null,
                textUpdater = placeholderContext,
                onClick = onClick
            )
        }
    )
}