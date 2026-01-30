package gg.aquatic.stacked.serialize

import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.option.AmountOptionHandle
import gg.aquatic.stacked.option.CustomModelDataLegacyOptionHandle
import gg.aquatic.stacked.option.CustomModelDataOptionHandle
import gg.aquatic.stacked.option.DamageOptionHandle
import gg.aquatic.stacked.option.DisplayNameOptionHandle
import gg.aquatic.stacked.option.DyeOptionHandle
import gg.aquatic.stacked.option.EnchantsOptionHandle
import gg.aquatic.stacked.option.FlagsOptionHandle
import gg.aquatic.stacked.option.ItemModelOptionHandle
import gg.aquatic.stacked.option.LoreOptionHandle
import gg.aquatic.stacked.option.MaxDamageOptionHandle
import gg.aquatic.stacked.option.MaxStackSizeOptionHandle
import gg.aquatic.stacked.option.RarityOptionHandle
import gg.aquatic.stacked.option.SpawnerTypeOptionHandle
import gg.aquatic.stacked.option.TooltipStyleOptionHandle
import gg.aquatic.stacked.option.UnbreakableOptionHandle
import org.bukkit.configuration.ConfigurationSection

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
}