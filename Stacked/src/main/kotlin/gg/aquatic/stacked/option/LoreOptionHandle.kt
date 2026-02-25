package gg.aquatic.stacked.option

import gg.aquatic.common.AquaticCommon
import gg.aquatic.stacked.Stacked
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

class LoreOptionHandle(
    val lore: List<Component>,
) : ItemOptionHandle {

    override val key = Companion.key
    override fun apply(itemMeta: ItemMeta) {
        itemMeta.lore(lore)
    }

    companion object : ItemOption {
        override val key = Key.key("itemoption:lore")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            if (!section.contains("lore")) return null
            val lore = section.getStringList("lore").map {
                AquaticCommon.miniMessage.parse(it).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
            }
            if (lore.isEmpty()) return null
            return LoreOptionHandle(lore)
        }
    }

}