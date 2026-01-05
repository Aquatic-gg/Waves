package gg.aquatic.stacked.option

import gg.aquatic.stacked.Stacked
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.meta.ItemMeta

class DisplayNameOptionHandle(
    displayName: Component
) : ItemOptionHandle {

    val displayName = displayName.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
    override val key = Companion.key

    override fun apply(itemMeta: ItemMeta) {
        itemMeta.displayName(displayName)
    }

    companion object : ItemOption {
        override val key = Key.key("itemoption:display-name")
        override fun load(section: ConfigurationSection): ItemOptionHandle? {
            val displayName = section.getString("display-name") ?: return null
            return DisplayNameOptionHandle(Stacked.miniMessage.deserialize(displayName))
        }
    }
}