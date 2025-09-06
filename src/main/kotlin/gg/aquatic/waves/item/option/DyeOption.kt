package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object DyeOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val color = section.getString("dye")?.split(";") ?: return null
        if (color.size != 3) return null
        return ItemOptionHandle.create( { item: ItemStack ->
            item.setData(
                DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(
                    Color.fromRGB(color[0].toInt(), color[1].toInt(), color[2].toInt()),
                    false
                )
            )
        })
    }
}