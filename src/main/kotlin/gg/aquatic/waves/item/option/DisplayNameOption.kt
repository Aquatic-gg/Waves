package gg.aquatic.waves.item.option

import gg.aquatic.waves.util.toMMComponent
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

object DisplayNameOption: ItemOption {
    override fun load(section: ConfigurationSection): ItemOptionHandle? {
        val displayName = section.getString("display-name") ?: return null
        return ItemOptionHandle.create { itemMeta: ItemMeta -> itemMeta.displayName(displayName.toMMComponent()) }
    }
}