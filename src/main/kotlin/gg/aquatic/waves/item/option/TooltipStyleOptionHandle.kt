package gg.aquatic.waves.item.option

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class TooltipStyleOptionHandle(
    val tooltipStyle: Key
): ItemOptionHandle {
    override val key = Companion.key
    override fun apply(itemStack: ItemStack) {
        itemStack.setData(DataComponentTypes.TOOLTIP_STYLE, tooltipStyle)
    }
    companion object: ItemOption {
        override val key = Key.key("itemoption:tooltip-style")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val tooltipStyle = section.getString("tooltip-style") ?: return null
            return TooltipStyleOptionHandle(Key.key(tooltipStyle))
        }
    }
}