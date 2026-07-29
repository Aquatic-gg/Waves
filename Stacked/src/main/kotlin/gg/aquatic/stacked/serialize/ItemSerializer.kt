package gg.aquatic.stacked.serialize

import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.option.*
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

abstract class ItemSerializer<T: StackedItem<T>> {

    companion object {
        val optionFactories = hashSetOf(
            AmountOptionHandle.Companion,
            CustomModelDataLegacyOptionHandle.Companion,
            CustomModelDataOptionHandle.Companion,
            DamageOptionHandle.Companion,
            DisplayNameOptionHandle.Companion,
            DyeOptionHandle.Companion,
            EnchantsOptionHandle.Companion,
            FlagsOptionHandle.Companion,
            ItemModelOptionHandle.Companion,
            LoreOptionHandle.Companion,
            MaxDamageOptionHandle.Companion,
            MaxStackSizeOptionHandle.Companion,
            RarityOptionHandle.Companion,
            SpawnerTypeOptionHandle.Companion,
            TooltipStyleOptionHandle.Companion,
            UnbreakableOptionHandle.Companion
        )
    }

    protected fun loadOptions(section: ConfigurationSection): List<ItemOptionHandle> {
        return optionFactories.mapNotNull { it.load(section) }
    }

    abstract fun fromSection(
        section: ConfigurationSection?
    ): T?

    inline fun <reified A : Any> fromSection(
        section: ConfigurationSection?, crossinline mapper: (ConfigurationSection, T) -> A
    ): A? {
        val item = fromSection(section) ?: return null
        return mapper(section!!, item)
    }

    fun fromSections(sections: List<ConfigurationSection>): List<T> {
        return sections.mapNotNull { fromSection(it) }
    }

    inline fun <reified A : Any> fromSections(
        sections: List<ConfigurationSection>,
        crossinline mapper: (ConfigurationSection, T) -> A
    ): List<A> {
        return sections.mapNotNull { fromSection(it, mapper) }
    }

    protected fun resolveBaseStack(input: String): ItemStack? {
        if (!input.contains(":")) {
            return Material.matchMaterial(input.uppercase())?.let { ItemStack(it) }
        }

        val factory = input.substringBefore(":")
        val id = input.substringAfter(":")

        return StackedItem.ITEM_FACTORIES[factory]?.create(id)
    }
}