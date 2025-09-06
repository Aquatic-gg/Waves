package gg.aquatic.waves.item.option

import gg.aquatic.waves.util.toMMComponent
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

object LoreOption : ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        if (!section.contains("lore")) return null
        val lore = section.getStringList("lore")
        return ItemOptionHandle.create { itemMeta: ItemMeta -> itemMeta.lore(lore.map { it.toMMComponent() }) }
    }
}