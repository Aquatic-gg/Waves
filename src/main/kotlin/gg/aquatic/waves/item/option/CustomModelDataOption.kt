package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object CustomModelDataOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val section = section.getConfigurationSection("custom-model-data") ?: return null
        val colorIds = section.getStringList("colors")

        val colors = ArrayList<Color>()
        for (id in colorIds) {
            val strs = id.split(";")
            if (strs.size != 3) continue
            colors += Color.fromRGB(strs[0].toInt(), strs[1].toInt(), strs[2].toInt())
        }
        val floats = section.getFloatList("floats")
        val flags = section.getBooleanList("flags")
        val strings = section.getStringList("strings")

        if (colors.isEmpty() && floats.isEmpty() && flags.isEmpty() && strings.isEmpty()) return null

        return ItemOptionHandle.create( { item: ItemStack ->
            item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                    .addColors(colors)
                    .addFloats(floats)
                    .addFlags(flags)
                    .addStrings(strings)
            )
        })
    }
}