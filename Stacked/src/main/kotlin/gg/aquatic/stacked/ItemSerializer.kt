package gg.aquatic.stacked

import gg.aquatic.stacked.option.*
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class ItemSerializer<T: StackedItem<T>>(
    val handler: ItemHandler<T>,
) {
    val optionFactories = hashSetOf(
        AmountOptionHandle,
        CustomModelDataLegacyOptionHandle,
        CustomModelDataOptionHandle,
        DamageOptionHandle,
        DisplayNameOptionHandle,
        DyeOptionHandle,
        EnchantsOptionHandle,
        FlagsOptionHandle,
        ItemModelOptionHandle,
        LoreOptionHandle,
        MaxDamageOptionHandle,
        MaxStackSizeOptionHandle,
        RarityOptionHandle,
        SpawnerTypeOptionHandle,
        TooltipStyleOptionHandle,
        UnbreakableOptionHandle
    )

    inline fun <reified A : Any> fromSection(
        section: ConfigurationSection?, crossinline mapper: (ConfigurationSection, T) -> A
    ): A? {
        val item = fromSection(section) ?: return null

        return mapper(section!!, item)
    }

    fun fromSection(
        section: ConfigurationSection?
    ): T? {
        section ?: return null
        return try {
            val material = section.getString("material", "STONE")!!
            val options = optionFactories.mapNotNull { it.load(section) }

            return create(
                material,
                options,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    private fun create(
        namespace: String,
        options: List<ItemOptionHandle>
    ): T? {
        val itemStack = if (namespace.contains(":")) {
            val id = namespace.split(":").first().uppercase()

            val factory = StackedItem.ITEM_FACTORIES[id] ?: return null
            factory.create(namespace.substring(id.length + 1))
        } else {
            ItemStack(Material.valueOf(namespace.uppercase()))
        } ?: return null

        return handler.create(
            itemStack,
            options
        )
    }
}