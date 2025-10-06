package gg.aquatic.replace.placeholder

import gg.aquatic.replace.PLACEHOLDER_REGEX
import gg.aquatic.replace.findPlaceholders
import gg.aquatic.replace.replacePlaceholders
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import kotlin.reflect.KProperty

class PlaceholderContext<T>(
    placeholders: Collection<Placeholder<T>>,
    val maxUpdateInterval: Int
) {
    companion object {
        val player: PlaceholderContext<Player>
            get() {
                return Placeholders.resolverFor(Player::class.java)
            }
    }

    val placeholders = placeholders.associateBy { it.identifier }.toMutableMap()

    fun addPlaceholder(placeholder: Placeholder<T>) {
        placeholders[placeholder.identifier] = placeholder
    }

    fun addPlaceholders(placeholders: Collection<Placeholder<T>>) {
        this.placeholders.putAll(placeholders.associateBy { it.identifier })
    }

    fun addPlaceholders(vararg placeholders: Placeholder<T>) {
        this.placeholders.putAll(placeholders.associateBy { it.identifier })
    }

    private val cache = HashMap<String, PlaceholderState>()

    private fun tryApply(binder: T, item: ComponentItem, firstTime: Boolean): Result<Component> {
        val original = item.original
        val latestComponent = item.latestState.value
        val foundPlaceholders = item.foundPlaceholders

        val (result, updated) = generateResult(binder, foundPlaceholders)

        if (!updated && !firstTime) {
            return Result(latestComponent, false)
        }
        val updatedComponent = original.replacePlaceholders(result)
        return Result(updatedComponent, (updatedComponent != latestComponent))
    }

    private fun tryApply(binder: T, item: LiteralItem, firstTime: Boolean): Pair<String, Boolean> {
        val original = item.original
        val latestString = item.latestState.value
        val foundPlaceholders = item.foundPlaceholders

        val (result, updated) = generateResult(binder, foundPlaceholders)

        if (!updated && !firstTime) {
            return latestString to false
        }

        var updatedString = original
        for ((placeholder, value) in result) {
            updatedString = updatedString.replace("%$placeholder%", value)
        }

        return updatedString to (updatedString != latestString)
    }

    private fun generateResult(binder: T, foundPlaceholders: List<String>): Pair<Map<String, String>, Boolean> {
        val currentTime = System.currentTimeMillis()
        val result = HashMap<String, String>()

        var updated = false

        for (string in foundPlaceholders) {
            val identifier = string.substringBefore("_")
            val placeholder = placeholders[identifier] ?: continue

            val previousValue = cache[string]

            if (previousValue != null && ((currentTime - previousValue.time) < (maxUpdateInterval * 50) || placeholder.isConst)) {
                result[string] = previousValue.updated
                continue
            }

            val newValue = placeholder.apply(binder, string)

            if (previousValue?.updated != newValue) {
                result[string] = newValue
                cache[string] = PlaceholderState(newValue, currentTime)
                updated = true
            } else {
                previousValue.time = currentTime
                result[string] = previousValue.updated
            }
        }
        return result to updated
    }

    /**
     * Creates a new `LiteralItem` instance based on the provided input `literal` and updates its state
     * using the supplied `binder`.
     *
     * @param binder the context or object used to*/
    fun createItem(binder: T, literal: String): LiteralItem {
        val result = mutableSetOf<String>()
        PLACEHOLDER_REGEX.findAll(literal).forEach { match ->
            result.add(match.groupValues[1]) // only the inside of %...%
        }
        return LiteralItem(literal, result.toList(), binder)
    }

    /**
     * Creates a new `Item` instance by identifying and processing placeholders within the given component.
     * This method filters placeholders based on a predefined set and updates their state using the provided binder.
     *
     * @param binder The context or object used to apply transformations or updates on the placeholders.
     * @param component The component containing the placeholders to be identified and processed.
     * @return A new `Item` instance with the processed component and found placeholders.
     */
    fun createItem(binder: T, component: Component): ComponentItem {
        val foundPlaceholders =
            component.findPlaceholders().filter { placeholders.containsKey(it.substringBefore("_").lowercase()) }

        return ComponentItem(component, foundPlaceholders, binder)
    }

    /**
     * Creates a new `ItemStackItem` instance by processing placeholders within the provided `ItemStack`.
     * This method analyzes and updates the display name and lore of the `ItemStack` using the supplied binder.
     *
     * @param binder The context or object used to apply transformations or updates on the placeholders.
     * @param itemStack The `ItemStack` to be processed, containing placeholders to be identified and handled.
     * @return A new `ItemStackItem` containing the processed state of the original `ItemStack`.
     */
    fun createItem(binder: T, itemStack: ItemStack): ItemStackItem {
        val meta = itemStack.itemMeta
        val name = meta?.displayName()
        val lore = meta?.lore()

        var foundPlaceholdersName = false
        var foundPlaceholdersLore = false
        val nameItem = name?.let {
            val item = createItem(binder, it)
            foundPlaceholdersName = item.foundPlaceholders.isNotEmpty()
            item
        }
        val loreItem = lore?.map {
            val item = createItem(binder, it)
            if (item.foundPlaceholders.isNotEmpty()) {
                foundPlaceholdersLore = true
            }
            item
        }

        return ItemStackItem(itemStack, meta, loreItem, nameItem, foundPlaceholdersName, foundPlaceholdersLore)
    }


    interface Item<A, B> {
        val original: A

        val latestState: Result<A>
        val isStatic: Boolean

        fun tryUpdate(binder: B): Result<A>
    }

    inner class LiteralItem(
        override val original: String,
        val foundPlaceholders: List<String>,
        binder: T
    ) : Item<String, T> {

        override var latestState: Result<String> = Result(original, false)
            private set

        init {
            if (foundPlaceholders.isNotEmpty()) {
                val (str, wasUpdated) = tryApply(binder, this, true)
                latestState = Result(str, wasUpdated)
            }
        }

        override val isStatic: Boolean = foundPlaceholders.isEmpty()

        /**
         * Updates the latest state of the `LiteralItem` with a potentially new string representation.
         *
         * The method attempts to apply placeholders using the provided binder. If placeholders are found and updated,
         * the latest state is refreshed with the new string and update status. If no placeholders are found, the current
         * latest state is returned without changes.
         *
         * @param binder The entity or context used to resolve and replace placeholders.
         * @param firstTime Indicates if this is the first update attempt, potentially influencing update behavior.
         * @return The updated `LiteralResult` containing the new string and whether it was updated.
         */
        override fun tryUpdate(
            binder: T
        ): Result<String> {
            if (foundPlaceholders.isEmpty()) return latestState
            val (str, wasUpdated) = tryApply(binder, this, false)
            latestState = Result(str, wasUpdated)
            return latestState
        }
    }

    inner class ComponentItem(
        override val original: Component,
        val foundPlaceholders: List<String>,
        binder: T
    ) : Item<Component, T> {

        override var latestState = Result(original, false)

        init {
            if (foundPlaceholders.isNotEmpty()) {
                val result = tryApply(binder, this, true)
                latestState = result
            }
        }

        override val isStatic: Boolean = foundPlaceholders.isEmpty()

        /**
         * Attempts to update the current state using the provided binder and placeholder data.
         * If no placeholders are found, the latest state is returned unmodified.
         *
         * @param binder The binding context of type `T` used to apply changes to placeholders.
         * @param firstTime Indicates whether this update attempt is the first one. If true, forces an update regardless of prior conditions.
         * @return A `Result` object containing the updated component and a flag indicating whether any changes were made.
         */
        override fun tryUpdate(binder: T): Result<Component> {
            if (foundPlaceholders.isEmpty()) return latestState
            val result = tryApply(binder, this, false)
            latestState = result
            return result
        }
    }

    inner class ItemStackItem(
        override val original: ItemStack,
        private val meta: ItemMeta?,
        val lore: List<ComponentItem>?,
        val name: ComponentItem?,
        val foundPlaceholdersName: Boolean,
        val foundPlaceholdersLore: Boolean
    ) : Item<ItemStack, T> {

        override var latestState = Result(original, false)
            private set

        init {
            if ((foundPlaceholdersName || foundPlaceholdersLore) && meta != null) {
                var nameUpdated = false
                var loreUpdated = false

                if (foundPlaceholdersLore) {
                    lore?.let {
                        for (item in it) {
                            val result = item.latestState
                            if (result.wasUpdated) {
                                loreUpdated = true
                                break
                            }
                        }
                    }
                }

                if (foundPlaceholdersName) {
                    name?.let {
                        val result = it.latestState
                        if (result.wasUpdated) {
                            nameUpdated = true
                        }
                    }
                }

                if ((nameUpdated || loreUpdated)) {
                    if ((nameUpdated) && name != null) {
                        meta.displayName(name.latestState.value)
                    }
                    if ((loreUpdated) && lore != null) {
                        meta.lore(lore.map { item -> item.latestState.value })
                    }
                    val item = latestState.value.clone()
                    item.itemMeta = meta

                    latestState = Result(item, true)
                } else {
                    latestState = Result(latestState.value, false)
                }
            }
        }

        override val isStatic: Boolean = (foundPlaceholdersName || foundPlaceholdersLore) && meta != null

        /**
         * Attempts to update the current item's state (name, lore, etc.) based on the provided binder and update conditions.
         * If placeholders for name or lore are not found, or if the meta data is null, the item remains unmodified.
         *
         * @param binder The binding context of type `T` used to apply changes to any dynamic placeholders.
         * @param firstTime Indicates whether this is the first update attempt. If true, forces an update regardless of prior conditions.
         * @return A `Result` object containing the updated `ItemStack` and a flag (`wasUpdated`) indicating whether any updates were applied.
         */
        override fun tryUpdate(binder: T): Result<ItemStack> {
            val meta = this.meta
            if ((!foundPlaceholdersName && !foundPlaceholdersLore) || meta == null) {
                return Result(latestState.value, false)
            }
            var nameUpdated = false
            var loreUpdated = false

            if (foundPlaceholdersLore) {
                lore?.let {
                    for (item in it) {
                        val result = item.tryUpdate(binder)
                        if (result.wasUpdated) {
                            loreUpdated = true
                        }
                    }
                }
            }

            if (foundPlaceholdersName) {
                name?.let {
                    val result = it.tryUpdate(binder)
                    if (result.wasUpdated) {
                        nameUpdated = true
                    }
                }
            }

            if ((nameUpdated || loreUpdated)) {
                if ((nameUpdated) && name != null) {
                    meta.displayName(name.latestState.value)
                }
                if ((loreUpdated) && lore != null) {
                    meta.lore(lore.map { item -> item.latestState.value })
                }
                val item = latestState.value.clone()
                item.itemMeta = meta

                val result = Result(item, true)
                latestState = result
                return result
            } else {
                val result = Result(latestState.value, false)
                return result
            }
        }
    }

    private class PlaceholderState(
        val updated: String,
        var time: Long
    )

    class Result<T>(
        val value: T,
        val wasUpdated: Boolean
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value
        }
    }
}