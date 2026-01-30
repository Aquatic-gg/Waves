package gg.aquatic.stacked.serialize

import gg.aquatic.stacked.ItemHandler
import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.impl.StackedItemImpl
import gg.aquatic.stacked.option.AmountOptionHandle
import gg.aquatic.stacked.option.CustomModelDataLegacyOptionHandle
import gg.aquatic.stacked.option.CustomModelDataOptionHandle
import gg.aquatic.stacked.option.DamageOptionHandle
import gg.aquatic.stacked.option.DisplayNameOptionHandle
import gg.aquatic.stacked.option.DyeOptionHandle
import gg.aquatic.stacked.option.EnchantsOptionHandle
import gg.aquatic.stacked.option.FlagsOptionHandle
import gg.aquatic.stacked.option.ItemModelOptionHandle
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.LoreOptionHandle
import gg.aquatic.stacked.option.MaxDamageOptionHandle
import gg.aquatic.stacked.option.MaxStackSizeOptionHandle
import gg.aquatic.stacked.option.RarityOptionHandle
import gg.aquatic.stacked.option.SpawnerTypeOptionHandle
import gg.aquatic.stacked.option.TooltipStyleOptionHandle
import gg.aquatic.stacked.option.UnbreakableOptionHandle
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class ItemSerializerImpl(
): ItemSerializer<StackedItemImpl>() {

    override fun fromSection(
        section: ConfigurationSection?
    ): StackedItemImpl? {
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

    private fun create(
        namespace: String,
        options: List<ItemOptionHandle>
    ): StackedItemImpl? {
        val itemStack = if (namespace.contains(":")) {
            val id = namespace.split(":").first().uppercase()

            val factory = StackedItem.ITEM_FACTORIES[id] ?: return null
            factory.create(namespace.substring(id.length + 1))
        } else {
            ItemStack(Material.valueOf(namespace.uppercase()))
        } ?: return null

        return ItemHandler.Impl.create(
            itemStack,
            options
        )
    }
}