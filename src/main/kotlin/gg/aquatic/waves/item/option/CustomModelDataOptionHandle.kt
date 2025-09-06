package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class CustomModelDataOptionHandle(
    val colors: List<Color>,
    val floats: List<Float>,
    val flags: List<Boolean>,
    val strings: List<String>
): ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(
            DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData()
                .addColors(colors)
                .addFloats(floats)
                .addFlags(flags)
                .addStrings(strings)
        )
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:model-data")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val section = section.getConfigurationSection("model-data") ?: return null
            val colors = section.getStringList("colors").mapNotNull {
                val strs = it.split(";")
                if (strs.size != 3) return@mapNotNull null
                Color.fromRGB(strs[0].toInt(), strs[1].toInt(), strs[2].toInt())
            }
            val floats = section.getStringList("floats").map { it.toFloat() }
            val flags = section.getStringList("flags").map { it.toBoolean() }
            val strings = section.getStringList("strings")

            if (colors.isEmpty() && floats.isEmpty() && flags.isEmpty() && strings.isEmpty()) return null
            return CustomModelDataOptionHandle(colors, floats, flags, strings)
        }

    }
}