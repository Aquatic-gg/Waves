package gg.aquatic.stacked.option

import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

class HideTooltipOptionHandle(
    val hidden: Boolean,
) : ItemOptionHandle {

    override val key = Companion.key

    override fun apply(itemMeta: ItemMeta) {
        itemMeta.isHideTooltip = hidden
    }

    companion object : ItemOption {
        override val key = Key.key("itemoption:hidetooltip")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("hidetooltip")) return null
            val hidden = section.getBoolean("hidetooltip")
            return HideTooltipOptionHandle(hidden)
        }
    }

}