package gg.aquatic.stacked.serialize

import gg.aquatic.stacked.ItemHandler
import gg.aquatic.stacked.impl.StackedItemImpl
import gg.aquatic.stacked.option.ItemOptionHandle
import org.bukkit.configuration.ConfigurationSection

object ItemSerializerImpl: ItemSerializer<StackedItemImpl>() {

    override fun fromSection(
        section: ConfigurationSection?
    ): StackedItemImpl? {
        section ?: return null
        return try {
            val material = section.getString("material", "STONE")!!
            val options = loadOptions(section)

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
        val itemStack = resolveBaseStack(namespace) ?: return null
        return ItemHandler.Impl.create(
            itemStack,
            options
        )
    }
}