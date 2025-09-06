package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.ItemHandler
import gg.aquatic.waves.item.option.AmountOption
import gg.aquatic.waves.item.option.CustomModelDataLegacyOption
import gg.aquatic.waves.item.option.CustomModelDataOption
import gg.aquatic.waves.item.option.DamageOption
import gg.aquatic.waves.item.option.DisplayNameOption
import gg.aquatic.waves.item.option.DyeOption
import gg.aquatic.waves.item.option.EnchantsOption
import gg.aquatic.waves.item.option.FlagsOption
import gg.aquatic.waves.item.option.ItemModelOption
import gg.aquatic.waves.item.option.ItemOptionHandle
import gg.aquatic.waves.item.option.LoreOption
import gg.aquatic.waves.item.option.MaxDamageOption
import gg.aquatic.waves.item.option.MaxStackSizeOption
import gg.aquatic.waves.item.option.RarityOption
import gg.aquatic.waves.item.option.SpawnerTypeOption
import gg.aquatic.waves.item.option.TooltipStyleOption
import gg.aquatic.waves.item.option.UnbreakableOption
import gg.aquatic.waves.registry.WavesRegistry
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ItemSerializer {

    val optionFactories = hashSetOf(
        AmountOption,
        CustomModelDataLegacyOption,
        CustomModelDataOption,
        DamageOption,
        DisplayNameOption,
        DyeOption,
        EnchantsOption,
        FlagsOption,
        ItemModelOption,
        LoreOption,
        MaxDamageOption,
        MaxStackSizeOption,
        RarityOption,
        SpawnerTypeOption,
        TooltipStyleOption,
        UnbreakableOption
    )

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection?, crossinline mapper: (ConfigurationSection, AquaticItem) -> T
    ): T? {
        val item = fromSection(section) ?: return null

        return mapper(section!!, item)
    }

    fun fromSection(
        section: ConfigurationSection?
    ): AquaticItem? {
        section ?: return null
        return try {
            val material = section.getString("material", "STONE")!!
            val options = optionFactories.mapNotNull { it.load(section) }

            return create(
                material,
                options
            )
        } catch (_: Exception) {
            null
        }
    }

    fun fromSections(sections: List<ConfigurationSection>): List<AquaticItem> {
        return sections.mapNotNull { fromSection(it) }
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        crossinline mapper: (ConfigurationSection, AquaticItem) -> T
    ): List<T> {
        return sections.mapNotNull { fromSection(it, mapper) }
    }

    private fun create(
        namespace: String,
        options: List<ItemOptionHandle>
    ): AquaticItem? {
        val itemStack = if (namespace.contains(":")) {
            val id = namespace.split(":").first().uppercase()
            val factory = WavesRegistry.ITEM_FACTORIES[id] ?: return null
            factory.create(namespace.substring(id.length + 1))
        } else {
            ItemStack(Material.valueOf(namespace.uppercase()))
        } ?: return null

        return ItemHandler.create(
            namespace,
            itemStack,
            options
        )
    }

}