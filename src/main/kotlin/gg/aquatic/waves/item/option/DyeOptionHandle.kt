package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class DyeOptionHandle(
    val color: Color,
) : ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color, false))
    }

    companion object : ItemOption {
        override val key = Key.key("itemoption:dye")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val colorStr = section.getString("dye") ?: return null
            val split = colorStr.split(";")
            if (split.size != 3) return null
            val color = Color.fromRGB(split[0].toInt(), split[1].toInt(), split[2].toInt())
            return DyeOptionHandle(color)
        }
    }
}