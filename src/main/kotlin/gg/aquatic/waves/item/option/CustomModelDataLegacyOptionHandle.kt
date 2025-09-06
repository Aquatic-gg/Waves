package gg.aquatic.waves.item.option

import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

class CustomModelDataLegacyOptionHandle(
    val modelData: Int
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemMeta: ItemMeta) {
        itemMeta.setCustomModelData(modelData)
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:model-data-legacy")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("model-data-legacy")) return null
            val modelData = section.getInt("model-data-legacy")
            return CustomModelDataLegacyOptionHandle(modelData)
        }
    }

}