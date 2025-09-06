package gg.aquatic.waves.item.option

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

object CustomModelDataLegacyOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("model-data-legacy")) return null
        val modelData = section.getInt("model-data-legacy")
        return ItemOptionHandle.create { itemMeta: ItemMeta ->
            itemMeta.setCustomModelData(modelData)
        }
    }
}