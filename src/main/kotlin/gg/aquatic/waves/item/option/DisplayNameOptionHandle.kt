package gg.aquatic.waves.item.option

import gg.aquatic.waves.util.toMMComponent
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

class DisplayNameOptionHandle(
    val displayName: String
): ItemOptionHandle {
    override val key = Companion.key

    override fun apply(itemMeta: ItemMeta) {
        itemMeta.displayName(displayName.toMMComponent())
    }

    companion object: ItemOption {
        override val key = Key.key("itemoption:display-name")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val displayName = section.getString("display-name") ?: return null
            return DisplayNameOptionHandle(displayName)
        }
    }

}