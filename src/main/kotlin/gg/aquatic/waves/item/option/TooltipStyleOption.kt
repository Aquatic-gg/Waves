package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

object TooltipStyleOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val style = section.getString("tooltip-style") ?: return null
        return ItemOptionHandle.create({ item: ItemStack ->
            item.setData(DataComponentTypes.TOOLTIP_STYLE, Key.key(style))
        })
    }
}